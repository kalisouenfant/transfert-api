package com.transfertapi.services;

import com.transfertapi.entities.Devise;
import com.transfertapi.repositories.DeviseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviseService {

    @Autowired
    private DeviseRepository deviseRepository;

    // 🔹 Lister toutes les devises
    public List<Devise> getAll() {
        return deviseRepository.findAll();
    }

    // 🔹 Rechercher une devise par son code
    public Optional<Devise> getByCode(String code) {
        return deviseRepository.findByCode(code);
    }

    // 🔹 Enregistrer ou mettre à jour une devise
    public Devise save(Devise devise) {
        return deviseRepository.save(devise);
    }

    // 🔹 Supprimer une devise par son ID
    public void delete(Integer id) {
        deviseRepository.deleteById(id);
    }

    // 🔹 Vérifier l’existence par code
    public boolean existsByCode(String code) {
        return deviseRepository.existsByCode(code);
    }
}
