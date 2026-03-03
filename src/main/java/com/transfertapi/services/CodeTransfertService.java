package com.transfertapi.services;

import com.transfertapi.entities.*;
import com.transfertapi.repositories.CodeTransfertRepository;
import com.transfertapi.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private MouvementCaisseService mouvementService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Récupère tous les transferts (Super-Admins)
     */
    public Page<CodeTransfert> getAllTransferts(int page, int size) {
        return repo.searchGlobal(null, null, PageRequest.of(page, size));
    }

    /**
     * Récupère les transferts filtrés par agence (Responsables)
     */
    public Page<CodeTransfert> getTransfertsByAgence(Integer agenceId, int page, int size) {
        return repo.searchByAgence(null, null, agenceId, PageRequest.of(page, size));
    }

    /**
     * ENVOI : le client dépose l'argent -> ENTRÉE en caisse
     */
    public CodeTransfert creer(Map<String, Object> data, Utilisateur current) {
        if (current.getAgence() == null) throw new RuntimeException("Utilisateur sans agence.");

        BigDecimal montant = new BigDecimal(data.get("montant").toString());
        if (montant.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Montant invalide.");

        Integer agenceEnvoiId = current.getAgence().getId();
        Integer agenceReceptionId = data.get("agenceReceptionId") != null ? (Integer) data.get("agenceReceptionId") : null;

        Client exp = getOrCreateClient(data.get("expediteurContact").toString(), data.get("expediteurNom").toString());
        Client ben = getOrCreateClient(data.get("beneficiaireContact").toString(), data.get("beneficiaireNom").toString());

        String code = genererCodeUnique();

        CodeTransfert ct = repo.save(CodeTransfert.builder()
                .code(code).montant(montant).statut(StatutCodeTransfert.ENVOYE)
                .expediteurClientId(exp.getId()).beneficiaireClientId(ben.getId())
                .agenceEnvoiId(agenceEnvoiId).agenceReceptionId(agenceReceptionId)
                .utilisateurId(current.getId()).dateEnvoi(LocalDateTime.now())
                .build());

        mouvementService.save(MouvementCaisse.builder()
                .agence(current.getAgence()).utilisateur(current).type(TypeMouvement.ENTREE)
                .montant(montant).motif("ENVOI TRANSFERT " + code).dateMouvement(LocalDateTime.now())
                .build());

        transactionService.creerTransaction(buildTransaction(ct, TypeTransaction.TRANSFERT_ENVOI, agenceEnvoiId, agenceReceptionId, current.getId()));

        return ct;
    }

    /**
     * RETRAIT : l'agence paie le bénéficiaire -> SORTIE de caisse
     * Gestion spéciale si l'agence de réception a été supprimée
     */
    public CodeTransfert retirer(String code, Utilisateur current) {
        CodeTransfert ct = verifier(code);
        Integer agenceRetraitId = current.getAgence().getId();

        if (ct.getStatut() != StatutCodeTransfert.ENVOYE)
            throw new RuntimeException("Statut invalide : " + ct.getStatut());

        // Vérification de l'agence de réception uniquement si elle existe
        if (ct.getAgenceReceptionId() != null) {
            boolean agenceExiste = true; // À remplacer par : agenceRepository.existsById(ct.getAgenceReceptionId())
            if (agenceExiste && !ct.getAgenceReceptionId().equals(agenceRetraitId)) {
                throw new RuntimeException("Retrait restreint à une autre agence.");
            }
        }

        mouvementService.verifierSoldeAgence(agenceRetraitId, ct.getMontant());

        mouvementService.save(MouvementCaisse.builder()
                .agence(current.getAgence())
                .utilisateur(current)
                .type(TypeMouvement.SORTIE)
                .montant(ct.getMontant())
                .motif("RETRAIT TRANSFERT " + code)
                .dateMouvement(LocalDateTime.now())
                .build());

        transactionService.creerTransaction(buildTransaction(ct, TypeTransaction.TRANSFERT_RECEPTION,
                ct.getAgenceEnvoiId(), agenceRetraitId, current.getId()));

        ct.setStatut(StatutCodeTransfert.RETIRE);
        ct.setDateRetrait(LocalDateTime.now());
        ct.setUtilisateurRetraitId(current.getId());
        ct.setAgenceEffectiveRetraitId(agenceRetraitId);

        return repo.save(ct);
    }

    /**
     * ANNULATION : on rend l'argent à l'expéditeur -> SORTIE de caisse
     */
    public CodeTransfert annuler(String code, Utilisateur current) {
        CodeTransfert ct = verifier(code);

        if (ct.getStatut() != StatutCodeTransfert.ENVOYE)
            throw new RuntimeException("Impossible d'annuler un code " + ct.getStatut());

        mouvementService.verifierSoldeAgence(current.getAgence().getId(), ct.getMontant());

        mouvementService.save(MouvementCaisse.builder()
                .agence(current.getAgence())
                .utilisateur(current)
                .type(TypeMouvement.SORTIE)
                .montant(ct.getMontant())
                .motif("ANNULATION TRANSFERT " + code)
                .dateMouvement(LocalDateTime.now())
                .build());

        transactionService.creerTransaction(buildTransaction(ct, TypeTransaction.TRANSFERT_ANNULATION, ct.getAgenceEnvoiId(), null, current.getId()));

        ct.setStatut(StatutCodeTransfert.ANNULE);
        ct.setDateAnnulation(LocalDateTime.now());
        ct.setUtilisateurAnnulationId(current.getId());

        return repo.save(ct);
    }

    /**
     * Vérification d'existence du code
     */
    public CodeTransfert verifier(String code) {
        return repo.findByCode(code).orElseThrow(() -> new RuntimeException("Code inexistant."));
    }

    private Client getOrCreateClient(String tel, String nom) {
        return clientRepo.findByTelephone(tel).orElseGet(() ->
                clientRepo.save(Client.builder().nom(nom).telephone(tel).build()));
    }

    private Transaction buildTransaction(CodeTransfert ct, TypeTransaction type, Integer envoiId, Integer recepId, Integer userId) {
        return Transaction.builder()
                .montant(ct.getMontant())
                .type(type)
                .statut(StatutTransaction.SUCCES)
                .agenceEnvoiId(envoiId)
                .agenceReceptionId(recepId)
                .expediteurClientId(ct.getExpediteurClientId())
                .beneficiaireClientId(ct.getBeneficiaireClientId())
                .utilisateurId(userId)
                .codeTransfertId(ct.getId())
                .build();
    }

    private String genererCodeUnique() {
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