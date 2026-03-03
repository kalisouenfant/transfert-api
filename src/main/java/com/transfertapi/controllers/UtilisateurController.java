package com.transfertapi.controllers;

import com.transfertapi.dto.UtilisateurDTO;
import com.transfertapi.entities.*;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService service;

    /* =====================================================
       UTILITAIRE : UTILISATEUR CONNECTÉ
    ===================================================== */

    private Utilisateur getCurrentUser(Authentication auth) {
        return service.getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /* =====================================================
       CREATION UTILISATEUR
       SUPERADMIN -> toutes agences
       ADMIN -> uniquement son agence
    ===================================================== */

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> create(@RequestBody UtilisateurDTO dto,
                                    Authentication auth) {

        try {

            Utilisateur current = getCurrentUser(auth);

            // Sécurité ADMIN → ne peut créer que dans sa propre agence
            if (current.getRole() == Role.ADMIN) {

                if (current.getAgence() == null) {
                    return ResponseEntity.status(403)
                            .body(Map.of("message", "Agence non définie pour cet admin"));
                }

                dto.setAgenceId(current.getAgence().getId());
            }

            Utilisateur utilisateur = service.fromDTO(dto);
            service.save(utilisateur);

            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur créé avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    /* =====================================================
       MODIFICATION UTILISATEUR
    ===================================================== */

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody UtilisateurDTO dto,
                                    Authentication auth) {

        try {

            Utilisateur current = getCurrentUser(auth);

            dto.setId(id);

            // ADMIN → ne peut modifier que dans son agence
            if (current.getRole() == Role.ADMIN) {

                if (current.getAgence() == null) {
                    return ResponseEntity.status(403)
                            .body(Map.of("message", "Agence non définie"));
                }

                dto.setAgenceId(current.getAgence().getId());
            }

            Utilisateur utilisateur = service.fromDTO(dto);
            service.save(utilisateur);

            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur modifié avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    /* =====================================================
       SUPPRESSION UTILISATEUR
       Réservée au SUPERADMIN
    ===================================================== */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {

        try {

            service.delete(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur supprimé avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    /* =====================================================
       PAGINATION SECURISEE
    ===================================================== */

    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> getPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String role,
            Authentication auth) {

        Utilisateur current = getCurrentUser(auth);
        List<Utilisateur> list;

        if (current.getRole() == Role.SUPERADMIN) {
            list = service.getAll();
        } else {
            if (current.getAgence() == null) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Agence non définie"));
            }
            list = service.getByAgenceId(current.getAgence().getId());
        }

        list = list.stream()
                .filter(u -> search.isBlank() ||
                        u.getNom().toLowerCase().contains(search.toLowerCase()))
                .filter(u -> role.isBlank() ||
                        u.getRole().name().equalsIgnoreCase(role))
                .collect(Collectors.toList());

        int total = list.size();

        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);

        return ResponseEntity.ok(Map.of(
                "content", list.subList(from, to)
                        .stream()
                        .map(this::convertToMap)
                        .collect(Collectors.toList()),
                "totalElements", total,
                "page", page,
                "size", size
        ));
    }

    /* =====================================================
       LISTE MINIMALE (Dropdown)
    ===================================================== */

    @GetMapping("/minimal")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> getMinimal(Authentication auth) {

        Utilisateur current = getCurrentUser(auth);
        List<Utilisateur> users;

        if (current.getRole() == Role.SUPERADMIN) {
            users = service.getAll();
        } else {
            if (current.getAgence() == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            users = service.getByAgenceId(current.getAgence().getId());
        }

        return ResponseEntity.ok(users.stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "nom", u.getNom()
                ))
                .collect(Collectors.toList()));
    }

    /* =====================================================
       CONVERSION SECURISEE
    ===================================================== */

    private Map<String, Object> convertToMap(Utilisateur u) {

        Map<String, Object> map = new HashMap<>();

        map.put("id", u.getId());
        map.put("nom", u.getNom());
        map.put("email", u.getEmail());
        map.put("role", u.getRole().name());
        map.put("actif", u.isActif());

        if (u.getAgence() != null) {
            map.put("agence", Map.of(
                    "id", u.getAgence().getId(),
                    "nom", u.getAgence().getNom()
            ));
        } else {
            map.put("agence", null);
        }

        return map;
    }
}