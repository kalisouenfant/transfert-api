package com.transfertapi.services;

import com.transfertapi.dto.UtilisateurDTO;
import com.transfertapi.entities.*;
import com.transfertapi.repositories.AgenceRepository;
import com.transfertapi.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Récupérer utilisateur par email (utilisé pour authentification)
     */
    public Optional<Utilisateur> getByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    /**
     * Liste complète (SUPERADMIN uniquement côté contrôleur)
     */
    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    /**
     * Liste par agence
     */
    public List<Utilisateur> getByAgenceId(Integer agenceId) {
        return utilisateurRepository.findByAgenceId(agenceId);
    }

    /**
     * Récupérer par ID
     */
    public Optional<Utilisateur> getById(Integer id) {
        return utilisateurRepository.findById(id);
    }

    /**
     * Vérifier existence email
     */
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    /**
     * Sauvegarde simple
     */
    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Suppression
     */
    public void delete(Integer id) {
        utilisateurRepository.deleteById(id);
    }

    /**
     * Conversion DTO -> Entity avec validation métier
     */
    public Utilisateur fromDTO(UtilisateurDTO dto) {

        Utilisateur utilisateur;

        // 🔹 Cas modification
        if (dto.getId() != null) {
            utilisateur = utilisateurRepository.findById(dto.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Utilisateur introuvable."));
        }
        // 🔹 Cas création
        else {
            if (utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email déjà utilisé.");
            }
            utilisateur = new Utilisateur();
        }

        // =========================
        // Champs de base
        // =========================
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setActif(dto.getActif() != null ? dto.getActif() : true);

        // =========================
        // Gestion rôle
        // =========================
        Role role;
        try {
            role = Role.valueOf(dto.getRole());
        } catch (Exception e) {
            throw new IllegalArgumentException("Rôle invalide.");
        }
        utilisateur.setRole(role);

        // =========================
        // Gestion mot de passe
        // =========================
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasse(
                    passwordEncoder.encode(dto.getMotDePasse())
            );
        } else if (dto.getId() == null) {
            // Mot de passe obligatoire à la création
            throw new IllegalArgumentException(
                    "Mot de passe obligatoire pour la création."
            );
        }

        // =========================
        // Gestion agence
        // =========================
        if (role == Role.SUPERADMIN) {
            utilisateur.setAgence(null);
        } else {

            if (dto.getAgenceId() == null) {
                throw new IllegalArgumentException(
                        "L'agence est obligatoire pour ce rôle."
                );
            }

            Agence agence = agenceRepository.findById(dto.getAgenceId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Agence introuvable."));

            utilisateur.setAgence(agence);
        }

        return utilisateur;
    }
}