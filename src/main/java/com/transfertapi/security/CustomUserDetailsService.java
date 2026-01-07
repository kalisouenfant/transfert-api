package com.transfertapi.security;

import com.transfertapi.entities.Utilisateur;
import com.transfertapi.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;   // ✅ AJOUT ICI
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur non trouvé : " + email));

        // 🚨 AGENCE DÉSACTIVÉE = REFUS
        if (utilisateur.getAgence() != null && !utilisateur.getAgence().isActif()) {
            throw new DisabledException(
                    "Connexion refusée : l'agence '" +
                    utilisateur.getAgence().getNom() +
                    "' est désactivée."
            );
        }

        // 🚨 UTILISATEUR DÉSACTIVÉ
        if (!utilisateur.isActif()) {
            throw new DisabledException("Votre compte utilisateur est désactivé.");
        }

        String role = (utilisateur.getRole() != null)
                ? utilisateur.getRole().name()
                : "AGENT";

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles(role)
                .disabled(false)
                .build();
    }
}
