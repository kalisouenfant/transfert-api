package com.transfertapi.services;

import com.transfertapi.entities.*;
import com.transfertapi.exceptions.ResourceNotFoundException;
import com.transfertapi.repositories.CodeTransfertRepository;
import com.transfertapi.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@Transactional
public class CodeTransfertService {

    @Autowired
    private CodeTransfertRepository repo;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MouvementCaisseService mouvementService;

    public CodeTransfert creer(Map<String, Object> data) {

        BigDecimal montant = new BigDecimal(data.get("montant").toString());
        Integer agenceEnvoiId = (Integer) data.get("agenceEnvoiId");
        Integer utilisateurId = (Integer) data.get("utilisateurId");
        Integer agenceReceptionId = (Integer) data.get("agenceReceptionId");

        String expTel = data.get("expediteurContact").toString();
        String expNom = data.get("expediteurNom").toString();
        Client exp = clientRepo.findByTelephone(expTel).orElseGet(() ->
                clientRepo.save(Client.builder().nom(expNom).telephone(expTel).build())
        );

        String benTel = data.get("beneficiaireContact").toString();
        String benNom = data.get("beneficiaireNom").toString();
        Client ben = clientRepo.findByTelephone(benTel).orElseGet(() ->
                clientRepo.save(Client.builder().nom(benNom).telephone(benTel).build())
        );

        String code = genererCode();

        CodeTransfert ct = repo.save(
                CodeTransfert.builder()
                        .code(code)
                        .montant(montant)
                        .statut(StatutCodeTransfert.ENVOYE)
                        .expediteurClientId(exp.getId())
                        .beneficiaireClientId(ben.getId())
                        .agenceEnvoiId(agenceEnvoiId)
                        .agenceReceptionId(agenceReceptionId)
                        .utilisateurId(utilisateurId)
                        .dateEnvoi(LocalDateTime.now())
                        .build()
        );

        mouvementService.save(
                MouvementCaisse.builder()
                        .agence(new Agence(agenceEnvoiId))
                        .utilisateur(new Utilisateur(utilisateurId))
                        .type(TypeMouvement.ENTREE)
                        .montant(montant)
                        .motif("ENVOI TRANSFERT " + code)
                        .dateMouvement(LocalDateTime.now())
                        .build()
        );

        transactionService.creerTransaction(
                Transaction.builder()
                        .montant(montant)
                        .type(TypeTransaction.TRANSFERT_ENVOI)
                        .statut(StatutTransaction.SUCCES)
                        .agenceEnvoiId(agenceEnvoiId)
                        .agenceReceptionId(agenceReceptionId)
                        .expediteurClientId(exp.getId())
                        .beneficiaireClientId(ben.getId())
                        .utilisateurId(utilisateurId)
                        .codeTransfertId(ct.getId())
                        .build()
        );

        return ct;
    }

    public CodeTransfert retirer(String code, Integer userId, Integer agenceReceptionId) {

        CodeTransfert ct = verifier(code);

        if (ct.getStatut() != StatutCodeTransfert.ENVOYE)
            throw new RuntimeException("Action impossible : ce code est " + ct.getStatut());

        if (!agenceReceptionId.equals(ct.getAgenceReceptionId()))
            throw new RuntimeException("Vous ne pouvez retirer que dans votre agence.");

        mouvementService.verifierSoldeAgence(agenceReceptionId, ct.getMontant());

        transactionService.creerTransaction(
                Transaction.builder()
                        .montant(ct.getMontant())
                        .type(TypeTransaction.TRANSFERT_RECEPTION)
                        .statut(StatutTransaction.SUCCES)
                        .agenceEnvoiId(ct.getAgenceEnvoiId())
                        .agenceReceptionId(agenceReceptionId)
                        .expediteurClientId(ct.getExpediteurClientId())
                        .beneficiaireClientId(ct.getBeneficiaireClientId())
                        .utilisateurId(userId)
                        .codeTransfertId(ct.getId())
                        .build()
        );

        mouvementService.save(
                MouvementCaisse.builder()
                        .agence(new Agence(agenceReceptionId))
                        .utilisateur(new Utilisateur(userId))
                        .type(TypeMouvement.SORTIE)
                        .montant(ct.getMontant())
                        .motif("RETRAIT TRANSFERT " + code)
                        .dateMouvement(LocalDateTime.now())
                        .build()
        );

        ct.setStatut(StatutCodeTransfert.RETIRE);
        ct.setDateRetrait(LocalDateTime.now());
        ct.setAgenceReceptionId(agenceReceptionId);

        return repo.save(ct);
    }

    public CodeTransfert annuler(String code, Utilisateur current) {

        CodeTransfert ct = verifier(code);

        if (ct.getStatut() != StatutCodeTransfert.ENVOYE)
            throw new RuntimeException("Action impossible : déjà " + ct.getStatut());

        if (current.getRole() != Role.SUPERADMIN) {

            if (current.getAgence() == null ||
                    !current.getAgence().getId().equals(ct.getAgenceEnvoiId())) {
                throw new RuntimeException("Vous ne pouvez annuler qu'un transfert émis par votre agence.");
            }
        }

        mouvementService.verifierSoldeAgence(ct.getAgenceEnvoiId(), ct.getMontant());

        transactionService.creerTransaction(
                Transaction.builder()
                        .montant(ct.getMontant())
                        .type(TypeTransaction.TRANSFERT_ANNULATION)
                        .statut(StatutTransaction.SUCCES)
                        .agenceEnvoiId(ct.getAgenceEnvoiId())
                        .utilisateurId(current.getId())
                        .codeTransfertId(ct.getId())
                        .build()
        );

        mouvementService.save(
                MouvementCaisse.builder()
                        .agence(new Agence(ct.getAgenceEnvoiId()))
                        .utilisateur(new Utilisateur(current.getId()))
                        .type(TypeMouvement.SORTIE)
                        .montant(ct.getMontant())
                        .motif("ANNULATION TRANSFERT " + code)
                        .dateMouvement(LocalDateTime.now())
                        .build()
        );

        ct.setStatut(StatutCodeTransfert.ANNULE);
        ct.setDateAnnulation(LocalDateTime.now());
        ct.setUtilisateurAnnulationId(current.getId());

        return repo.save(ct);
    }

    public Page<CodeTransfert> listFiltered(String statut, String search, Integer agenceId, Pageable pageable) {

        StatutCodeTransfert statutEnum = null;

        if (statut != null && !statut.isEmpty() && !"TOUS".equalsIgnoreCase(statut)) {
            try { statutEnum = StatutCodeTransfert.valueOf(statut.toUpperCase()); }
            catch (Exception ignored) { }
        }

        String searchParam = (search == null || search.trim().isEmpty()) ? null : search;

        if (agenceId == null)
            return repo.searchGlobal(statutEnum, searchParam, pageable);

        return repo.searchByAgence(statutEnum, searchParam, agenceId, pageable);
    }

    public CodeTransfert verifier(String code) {
        return repo.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Code introuvable : " + code));
    }

    private String genererCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
                if ((i + 1) % 4 == 0 && i < 11) sb.append("-");
            }
            code = sb.toString();
        } while (repo.findByCode(code).isPresent());

        return code;
    }
}
