package com.transfertapi.controllers;

import com.transfertapi.dto.UtilisateurDTO;
import com.transfertapi.entities.*;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService service;

    private Utilisateur getCurrentUser(Authentication auth) {
        String email = auth.getName();
        return service.getByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé dans le système"));
    }

    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        List<Utilisateur> users;

        if (current.getRole() == Role.SUPERADMIN) {
            users = service.getAll();
        } else if (current.getRole() == Role.ADMIN) {
            users = service.getByAgenceId(current.getAgence().getId());
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé aux non-admins"));
        }

        return ResponseEntity.ok(
                users.stream().map(this::convertToMap).collect(Collectors.toList())
        );
    }

    // 🔹 NOUVEAU ENDPOINT : minimal sécurisé (AGENT/RESPONSABLE autorisés)
    @GetMapping("/minimal")
    public ResponseEntity<?> getMinimal(Authentication auth) {

        Utilisateur current = getCurrentUser(auth);

        // SUPERADMIN → tout
        if (current.getRole() == Role.SUPERADMIN) {
            return ResponseEntity.ok(
                    service.getAll()
                            .stream()
                            .map(u -> Map.of(
                                    "id", u.getId(),
                                    "nom", u.getNom()
                            ))
                            .collect(Collectors.toList())
            );
        }

        // TOUS LES AUTRES (ADMIN, RESPONSABLE, AGENT) → uniquement leur agence
        return ResponseEntity.ok(
                service.getByAgenceId(current.getAgence().getId())
                        .stream()
                        .map(u -> Map.of(
                                "id", u.getId(),
                                "nom", u.getNom()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UtilisateurDTO dto, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);

        if (current.getRole() != Role.SUPERADMIN && current.getRole() != Role.ADMIN)
            return ResponseEntity.status(403).body(Map.of("error", "Action interdite"));

        if (current.getRole() == Role.ADMIN) {
            dto.setAgenceId(current.getAgence().getId());
            if ("SUPERADMIN".equals(dto.getRole()))
                return ResponseEntity.badRequest().body(Map.of("error", "Création SuperAdmin interdite"));
        }

        if (service.existsByEmail(dto.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("error", "Email déjà utilisé"));

        Utilisateur u = service.fromDTO(dto);
        service.save(u);
        return ResponseEntity.ok(Map.of("message", "Utilisateur créé"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody UtilisateurDTO dto, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        Utilisateur target = service.getById(id)
                .orElseThrow(() -> new RuntimeException("Cible introuvable"));

        if (current.getRole() == Role.ADMIN &&
                (target.getAgence() == null || !target.getAgence().getId().equals(current.getAgence().getId()))) {
            return ResponseEntity.status(403).body(Map.of("error", "Interdit de modifier un utilisateur hors agence"));
        }

        dto.setId(id);
        if (current.getRole() == Role.ADMIN) {
            dto.setAgenceId(current.getAgence().getId());
        }

        Utilisateur updated = service.fromDTO(dto);
        service.save(updated);
        return ResponseEntity.ok(Map.of("message", "Mis à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        Optional<Utilisateur> targetOpt = service.getById(id);

        if (targetOpt.isEmpty()) return ResponseEntity.notFound().build();
        Utilisateur target = targetOpt.get();

        if (current.getRole() == Role.ADMIN) {
            if (target.getAgence() == null || !target.getAgence().getId().equals(current.getAgence().getId()))
                return ResponseEntity.status(403).body(Map.of("error", "Interdit de supprimer hors de votre agence"));
        } else if (current.getRole() != Role.SUPERADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Droit de suppression insuffisant"));
        }

        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé"));
    }

    private Map<String, Object> convertToMap(Utilisateur u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("nom", u.getNom());
        map.put("email", u.getEmail());
        map.put("role", u.getRole().name());
        map.put("actif", u.isActif());
        map.put("agence", u.getAgence() != null
                ? Map.of("id", u.getAgence().getId(), "nom", u.getAgence().getNom())
                : null);
        return map;
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String role,
            Authentication auth
    ) {

        Utilisateur current = getCurrentUser(auth);
        List<Utilisateur> list;

        if (current.getRole() == Role.SUPERADMIN) {
            list = service.getAll();
        } else if (current.getRole() == Role.ADMIN) {
            list = service.getByAgenceId(current.getAgence().getId());
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }

        list = list.stream()
                .filter(u -> search.isBlank()
                        || u.getNom().toLowerCase().contains(search.toLowerCase())
                        || u.getEmail().toLowerCase().contains(search.toLowerCase()))
                .filter(u -> role.isBlank() || u.getRole().name().equalsIgnoreCase(role))
                .collect(Collectors.toList());

        int total = list.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);

        List<Map<String, Object>> content = list.subList(from, to)
                .stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                Map.of(
                        "content", content,
                        "totalElements", total,
                        "page", page,
                        "size", size
                )
        );
    }
}
