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
        return service.getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String role,
            Authentication auth) {

        Utilisateur current = getCurrentUser(auth);
        List<Utilisateur> list;

        // Logique de filtrage par rôle
        if (current.getRole() == Role.SUPERADMIN) {
            list = service.getAll();
        } else if (current.getRole() == Role.ADMIN && current.getAgence() != null) {
            list = service.getByAgenceId(current.getAgence().getId());
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }

        // Filtrage manuel (Search & Role)
        list = list.stream()
                .filter(u -> search.isBlank() || u.getNom().toLowerCase().contains(search.toLowerCase()))
                .filter(u -> role.isBlank() || u.getRole().name().equalsIgnoreCase(role))
                .collect(Collectors.toList());

        int total = list.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);

        return ResponseEntity.ok(Map.of(
            "content", list.subList(from, to).stream().map(this::convertToMap).collect(Collectors.toList()),
            "totalElements", total,
            "page", page,
            "size", size
        ));
    }

    @GetMapping("/minimal")
    public ResponseEntity<?> getMinimal(Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        List<Utilisateur> users;

        if (current.getRole() == Role.SUPERADMIN) {
            users = service.getAll();
        } else if (current.getAgence() != null) {
            users = service.getByAgenceId(current.getAgence().getId());
        } else {
            users = Collections.emptyList();
        }

        return ResponseEntity.ok(users.stream()
                .map(u -> Map.of("id", u.getId(), "nom", u.getNom()))
                .collect(Collectors.toList()));
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
}