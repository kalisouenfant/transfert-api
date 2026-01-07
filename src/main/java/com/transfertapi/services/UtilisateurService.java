package com.transfertapi.services;

import com.transfertapi.dto.UtilisateurDTO;
import com.transfertapi.entities.*;
import com.transfertapi.repositories.AgenceRepository;
import com.transfertapi.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Nouvelle méthode pour sécuriser le contrôleur
    public Optional<Utilisateur> getByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    public List<Utilisateur> getByAgenceId(Integer agenceId) {
        return utilisateurRepository.findByAgenceId(agenceId);
    }

    public Optional<Utilisateur> getById(Integer id) {
        return utilisateurRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    public void delete(Integer id) {
        utilisateurRepository.deleteById(id);
    }

    public Utilisateur fromDTO(UtilisateurDTO dto) {
        Utilisateur u = (dto.getId() != null) 
            ? utilisateurRepository.findById(dto.getId()).orElse(new Utilisateur())
            : new Utilisateur();

        u.setNom(dto.getNom());
        u.setEmail(dto.getEmail());
        u.setActif(dto.getActif() != null ? dto.getActif() : true);
        u.setRole(Role.valueOf(dto.getRole()));

        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isBlank()) {
            u.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        }

        if (u.getRole() == Role.SUPERADMIN) {
            u.setAgence(null);
        } else {
            if (dto.getAgenceId() == null)
                throw new IllegalArgumentException("L'agence est obligatoire pour ce rôle.");
            
            Agence agence = agenceRepository.findById(dto.getAgenceId())
                    .orElseThrow(() -> new IllegalArgumentException("Agence introuvable."));
            u.setAgence(agence);
        }

        return u;
    }
}