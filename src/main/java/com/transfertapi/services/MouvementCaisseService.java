package com.transfertapi.services;

import com.transfertapi.dto.MouvementCaisseDTO;
import com.transfertapi.entities.*;
import com.transfertapi.exceptions.ResourceNotFoundException;
import com.transfertapi.repositories.AgenceRepository;
import com.transfertapi.repositories.MouvementCaisseRepository;
import com.transfertapi.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MouvementCaisseService {

    @Autowired
    private MouvementCaisseRepository repo;

    @Autowired
    private AgenceRepository agenceRepo;

    @Autowired
    private UtilisateurRepository userRepo;

    @Autowired
    private TransactionService transactionService;


    /* ==================  MAPPER  (ENTITE -> DTO)  ================== */
    public MouvementCaisseDTO toDTO(MouvementCaisse m) {
        return MouvementCaisseDTO.builder()
                .id(m.getId())
                .agenceId(m.getAgence().getId())
                .agenceNom(m.getAgence().getNom())
                .type(m.getType().name())
                .montant(m.getMontant())
                .motif(m.getMotif())
                .utilisateurId(m.getUtilisateur().getId())
                .utilisateurNom(m.getUtilisateur().getNom())
                .dateMouvement(m.getDateMouvement())
                .build();
    }


    /*==============================================================
                       FILTRAGE SANS PAGINATION
    ================================================================*/
    public List<MouvementCaisse> getFiltered(String type, Integer agenceId) {
        List<MouvementCaisse> list = repo.findAll();

        if (type != null && !type.equalsIgnoreCase("Tous")) {
            list.removeIf(m -> !m.getType().name().equalsIgnoreCase(type));
        }

        if (agenceId != null && agenceId > 0) {
            list.removeIf(m -> !m.getAgence().getId().equals(agenceId));
        }

        return list;
    }


    /*==============================================================
                         FILTRAGE AVEC PAGINATION
    ================================================================*/
    public Page<MouvementCaisse> getPagedFiltered(String type, Integer agenceId, Pageable pageable) {

        Specification<MouvementCaisse> spec = Specification.where(null);

        if (type != null && !type.equalsIgnoreCase("Tous")) {
            try {
                TypeMouvement typeEnum = TypeMouvement.valueOf(type.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), typeEnum));
            } catch (Exception ignored) {}
        }

        if (agenceId != null && agenceId > 0) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("agence").get("id"), agenceId));
        }

        return repo.findAll(spec, pageable);
    }


    /*==============================================================
               CREATION DEPUIS SWING  (DEPOT / RETRAIT MANUEL)
    ================================================================*/
    public MouvementCaisse createFromRequest(Map<String, Object> json) {

        Integer agenceId = safeInt(json.get("agenceId"));
        Integer userId = safeInt(json.get("utilisateurId"));
        String typeStr = safeString(json.get("type"));
        String motifEnumStr = safeString(json.get("motif"));

        if (agenceId == null) throw new IllegalArgumentException("Agence obligatoire.");
        if (userId == null) throw new IllegalArgumentException("Utilisateur obligatoire.");
        if (motifEnumStr == null) throw new IllegalArgumentException("Motif obligatoire.");
        if (typeStr == null) throw new IllegalArgumentException("Type obligatoire.");

        BigDecimal montant = new BigDecimal(json.get("montant").toString());

        TypeMouvement typeMouvement = TypeMouvement.valueOf(typeStr.toUpperCase());
        TypeMotifCaisse motifEnum = TypeMotifCaisse.valueOf(motifEnumStr.toUpperCase());
        String motifLibel = motifEnum.getLibel();

        Agence agence = agenceRepo.findById(agenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable"));

        Utilisateur user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (typeMouvement == TypeMouvement.SORTIE) {
            verifierSoldeAgence(agenceId, montant);
        }

        MouvementCaisse mouvement = MouvementCaisse.builder()
                .agence(agence)
                .utilisateur(user)
                .motif(motifLibel)
                .montant(montant)
                .type(typeMouvement)
                .dateMouvement(LocalDateTime.now())
                .build();

        MouvementCaisse saved = repo.save(mouvement);

        TypeTransaction transType =
                (typeMouvement == TypeMouvement.ENTREE)
                        ? TypeTransaction.DEPOT
                        : TypeTransaction.RETRAIT;

        Transaction transaction = Transaction.builder()
                .montant(montant)
                .type(transType)
                .utilisateurId(userId)
                .statut(StatutTransaction.SUCCES)
                .agenceReceptionId(agenceId)
                .build();

        transactionService.creerTransaction(transaction);

        return saved;
    }


    /*==============================================================
                     UTILISÉ PAR CodeTransfertService
    ================================================================*/
    public MouvementCaisse create(MouvementCaisse m) {

        if (m.getAgence() == null || m.getAgence().getId() == null)
            throw new IllegalArgumentException("Agence obligatoire.");

        if (m.getType() == TypeMouvement.SORTIE) {
            verifierSoldeAgence(m.getAgence().getId(), m.getMontant());
        }

        return repo.save(m);
    }

    public MouvementCaisse save(MouvementCaisse m) {
        return create(m);
    }


    /*==============================================================
                             SOLDE
    ================================================================*/
    public BigDecimal calculerSoldeAgence(Integer agenceId) {
        BigDecimal solde = repo.calculerSoldeAgence(agenceId);
        return solde != null ? solde : BigDecimal.ZERO;
    }

    public void verifierSoldeAgence(Integer agenceId, BigDecimal montantARetirer) {
        BigDecimal solde = calculerSoldeAgence(agenceId);

        if (solde.compareTo(montantARetirer) < 0) {
            throw new IllegalStateException(
                    "Solde insuffisant. Solde actuel: " + solde + " | Requis: " + montantARetirer
            );
        }
    }


    /*==============================================================
                             CRUD
    ================================================================*/
    public Optional<MouvementCaisse> getById(Integer id) {
        return repo.findById(id);
    }

    public List<MouvementCaisse> getAll() {
        return repo.findAll();
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }


    /*==============================================================
                        HELPERS
    ================================================================*/
    private Integer safeInt(Object o) {
        if (o == null) return null;
        return Integer.parseInt(o.toString());
    }

    private String safeString(Object o) {
        return (o == null) ? null : o.toString();
    }
}
