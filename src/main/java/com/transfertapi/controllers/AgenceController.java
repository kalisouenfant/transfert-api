package com.transfertapi.controllers;

import com.transfertapi.entities.Agence;
import com.transfertapi.entities.Role;
import com.transfertapi.entities.Utilisateur;
import com.transfertapi.services.AgenceService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agences")
@CrossOrigin(origins = "*")
public class AgenceController {

    @Autowired private AgenceService agenceService;
    @Autowired private UtilisateurService utilisateurService;

    private Utilisateur getCurrent(Authentication auth) {
        return utilisateurService.getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /** LISTE — superadmin/admin voient tout, responsables & agents voient seulement leur agence */
    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {

        Utilisateur user = getCurrent(auth);

        if (user.getRole() == Role.SUPERADMIN || user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(agenceService.getAll());
        }

        return ResponseEntity.ok(List.of(user.getAgence()));
    }

    /** CONSULTER UNE AGENCE */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id, Authentication auth) {

        Utilisateur user = getCurrent(auth);

        return agenceService.getById(id)
                .map(ag -> {

                    if (user.getRole() != Role.SUPERADMIN &&
                        user.getRole() != Role.ADMIN &&
                        !ag.getId().equals(user.getAgence().getId())) {

                        return ResponseEntity.status(403)
                                .body(Map.of("error", "Accès refusé"));
                    }

                    return ResponseEntity.ok(ag);
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Agence introuvable")));
    }

    /** CRÉATION — uniquement SUPERADMIN & ADMIN */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Agence agence, Authentication auth) {

        Utilisateur user = getCurrent(auth);

        if (user.getRole() != Role.SUPERADMIN && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Action interdite"));
        }

        if (agence.getNom() == null || agence.getNom().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nom est obligatoire"));
        }

        if (agenceService.existsByNom(agence.getNom())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ce nom existe déjà"));
        }

        return ResponseEntity.ok(
                Map.of("message", "Agence créée", "agence", agenceService.save(agence))
        );
    }

    /** MISE À JOUR — superadmin & admin */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody Agence agence,
                                    Authentication auth) {

        Utilisateur user = getCurrent(auth);

        if (user.getRole() != Role.SUPERADMIN && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Action interdite"));
        }

        return agenceService.getById(id)
                .map(existing -> {
                    agence.setId(id);
                    return ResponseEntity.ok(
                            Map.of("message", "Agence mise à jour", "agence", agenceService.save(agence))
                    );
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Agence introuvable")));
    }

    /** SUPPRESSION — uniquement SuperAdmin (avec contrôle d’usage) */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, Authentication auth) {

        Utilisateur user = getCurrent(auth);

        if (user.getRole() != Role.SUPERADMIN) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Action réservée au SuperAdmin"));
        }

        try {
            agenceService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Agence supprimée"));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
