package com.transfertapi.services;

import com.transfertapi.entities.Agence;
import com.transfertapi.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgenceService {

    @Autowired private AgenceRepository agenceRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private MouvementCaisseRepository mouvementRepository;
    @Autowired private CodeTransfertRepository codeTransfertRepository;

    public List<Agence> getAll() {
        return agenceRepository.findAll();
    }

    public Optional<Agence> getById(Integer id) {
        return agenceRepository.findById(id);
    }

    public Agence save(Agence agence) {
        return agenceRepository.save(agence);
    }

    /** Suppression protégée */
    public void delete(Integer id) {

        boolean used =
                utilisateurRepository.existsByAgenceId(id) ||
                mouvementRepository.existsByAgenceId(id) ||
                codeTransfertRepository.existsByAgenceEnvoiIdOrAgenceReceptionId(id, id);

        if (used) {
            throw new IllegalStateException(
                    "Impossible de supprimer : l'agence est encore liée à des opérations."
            );
        }

        agenceRepository.deleteById(id);
    }

    public boolean existsByNom(String nom) {
        return agenceRepository.existsByNom(nom);
    }
}
