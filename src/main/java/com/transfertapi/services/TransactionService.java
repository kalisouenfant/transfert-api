package com.transfertapi.services;

import com.transfertapi.entities.*;
import com.transfertapi.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository repo;
    private final JournalActionService journalService;

    public TransactionService(TransactionRepository repo, JournalActionService journalService) {
        this.repo = repo;
        this.journalService = journalService;
    }

    // ===============================
    //      CREATION TRANSACTION
    // ===============================
    public Transaction creerTransaction(Transaction t) {

        if (t.getMontant() == null || t.getMontant().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Montant invalide.");

        if (t.getType() == null)
            throw new IllegalArgumentException("Type obligatoire.");

        if (t.getUtilisateurId() == null)
            throw new IllegalArgumentException("Utilisateur obligatoire.");

        if (t.getAgenceEnvoiId() == null && t.getAgenceReceptionId() == null)
            throw new IllegalArgumentException("Agence obligatoire.");

        t.setDateTransaction(LocalDateTime.now());

        if (t.getStatut() == null)
            t.setStatut(StatutTransaction.SUCCES);

        Transaction saved = repo.save(t);

        // JOURNAL
        journalService.enregistrer(
                JournalAction.builder()
                        .utilisateurId(t.getUtilisateurId())
                        .action("TRANSACTION")
                        .description(
                                "Transaction " + t.getType() +
                                        " | montant=" + t.getMontant() +
                                        ", agenceEnvoi=" + t.getAgenceEnvoiId() +
                                        ", agenceReception=" + t.getAgenceReceptionId() +
                                        ", codeTransfertId=" + t.getCodeTransfertId() +
                                        ", date=" + LocalDateTime.now()
                        )
                        .ip(null)
                        .build()
        );

        return saved;
    }

    // ===============================
    //              CRUD
    // ===============================
    public List<Transaction> getAll() { 
        return repo.findAll(); 
    }

    public Optional<Transaction> getById(Integer id) { 
        return repo.findById(id); 
    }

    public void supprimer(Integer id) { 
        repo.deleteById(id); 
    }


    // ===============================
    //      🔎 FILTRAGE PAR ROLE
    // ===============================

    /** Transactions d'une agence (envoi OU réception) */
    public List<Transaction> getByAgence(Integer agenceId) {
        return repo.findByAgenceEnvoiIdOrAgenceReceptionId(agenceId, agenceId);
    }

    /** Transactions effectuées par un utilisateur précis */
    public List<Transaction> getByUser(Integer userId) {
        return repo.findByUtilisateurId(userId);
    }
}
