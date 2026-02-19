package com.transfertapi.security;

import com.transfertapi.entities.Utilisateur;
import com.transfertapi.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import indispensable

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true) // Garde la session ouverte pour charger l'agence
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

        // 1. Vérifier si le compte utilisateur lui-même est actif
        if (!utilisateur.isActif()) {
            throw new DisabledException("Compte utilisateur désactivé.");
        }

        // 2. Vérifier si l'agence est active (si l'utilisateur appartient à une agence)
        // Les SuperAdmin n'ont souvent pas d'agence (null), donc on vérifie la nullité
        if (utilisateur.getAgence() != null && !utilisateur.getAgence().isActif()) {
            throw new DisabledException("Accès refusé : Votre agence est inactive.");
        }

        // 3. Récupération du rôle (Enum name: ADMIN, AGENT, etc.)
        String roleName = utilisateur.getRole().name();

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .authorities("ROLE_" + roleName) // Spring Security attend le préfixe ROLE_
                .build();
    }
}