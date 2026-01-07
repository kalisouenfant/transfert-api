package com.transfertapi.controllers;

import com.transfertapi.dto.MouvementCaisseDTO;
import com.transfertapi.entities.*;
import com.transfertapi.exceptions.ResourceNotFoundException;
import com.transfertapi.services.MouvementCaisseService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/mouvements-caisse")
@CrossOrigin(origins = "*")
public class MouvementCaisseController {

    @Autowired
    private MouvementCaisseService service;

    @Autowired
    private UtilisateurService utilisateurService;

    /**
     * Récupère l'utilisateur connecté de manière sécurisée
     */
    private Utilisateur getCurrentUser(Authentication auth) {
        return utilisateurService.getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /* ========= PAGINATION AVEC RÔLES (ADMIN, AGENT, RESPONSABLE) + DTO ========= */
    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer agenceId,
            Authentication auth
    ) {
        Utilisateur current = getCurrentUser(auth);

        // Sécurité : Les rôles locaux ne voient que leur propre agence
        if (current.getRole() == Role.AGENT 
                || current.getRole() == Role.ADMIN 
                || current.getRole() == Role.RESPONSABLE) {
            agenceId = current.getAgence().getId();
        }

        Page<MouvementCaisseDTO> mapped =
                service.getPagedFiltered(type, agenceId, PageRequest.of(page, size))
                        .map(service::toDTO);

        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(service::toDTO)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Mouvement introuvable"));
    }

    @GetMapping("/solde/{agenceId}")
    public ResponseEntity<?> getSoldeAgence(@PathVariable Integer agenceId, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);

        // Un non-superadmin ne peut voir que le solde de sa propre agence
        if (current.getRole() != Role.SUPERADMIN &&
            !current.getAgence().getId().equals(agenceId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès au solde interdit"));
        }

        BigDecimal solde = service.calculerSoldeAgence(agenceId);
        return ResponseEntity.ok(Map.of("solde", solde));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> json, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        Integer agenceIdRequest = Integer.parseInt(json.get("agenceId").toString());

        // Interdiction d'enregistrer un mouvement pour une autre agence
        if (current.getRole() != Role.SUPERADMIN &&
            !current.getAgence().getId().equals(agenceIdRequest)) {
            return ResponseEntity.status(403).body(Map.of("error", "Action interdite pour cette agence"));
        }

        MouvementCaisse saved = service.createFromRequest(json);
        return ResponseEntity.ok(Map.of(
                "message", "Mouvement enregistré",
                "id", saved.getId()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);

        MouvementCaisse m = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mouvement introuvable"));

        // Seul le SuperAdmin ou l'Admin/Responsable de la propre agence peut supprimer
        if (current.getRole() != Role.SUPERADMIN &&
           ((current.getRole() != Role.ADMIN && current.getRole() != Role.RESPONSABLE) ||
            !m.getAgence().getId().equals(current.getAgence().getId()))) {

            return ResponseEntity.status(403).body(Map.of("error", "Suppression interdite"));
        }

        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Supprimé avec succès"));
    }
}