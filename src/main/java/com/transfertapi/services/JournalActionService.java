package com.transfertapi.services;

import com.transfertapi.entities.JournalAction;
import com.transfertapi.repositories.JournalActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalActionService {

    private final JournalActionRepository repo;

    public JournalActionService(JournalActionRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public JournalAction enregistrer(JournalAction action) {
        action.setDateAction(LocalDateTime.now());
        return repo.save(action);
    }

    public List<JournalAction> getAll() {
        return repo.findAll();
    }

    public Optional<JournalAction> getById(Integer id) {
        return repo.findById(id);
    }

    public List<JournalAction> getByUtilisateur(Integer utilisateurId) {
        return repo.findByUtilisateurId(utilisateurId);
    }

    public List<JournalAction> rechercherParAction(String motCle) {
        return repo.findByActionContainingIgnoreCase(motCle);
    }

    @Transactional
    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
