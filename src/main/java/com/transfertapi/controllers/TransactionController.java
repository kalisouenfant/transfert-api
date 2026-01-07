package com.transfertapi.controllers;

import com.transfertapi.entities.Role;
import com.transfertapi.entities.Transaction;
import com.transfertapi.entities.Utilisateur;
import com.transfertapi.exceptions.ResourceNotFoundException;
import com.transfertapi.services.TransactionService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService service;

    @Autowired
    private UtilisateurService utilisateurService;


    private Utilisateur getCurrentUser(Authentication auth) {
        return utilisateurService.getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /* ===================== CREER ===================== */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Transaction transaction,
                                    Authentication auth) {

        Utilisateur current = getCurrentUser(auth);

        // Un non-superadmin ne peut enregistrer que pour son agence
        if (current.getRole() != Role.SUPERADMIN) {

            Integer agenceUser = current.getAgence().getId();

            if (transaction.getAgenceEnvoiId() != null &&
                !transaction.getAgenceEnvoiId().equals(agenceUser)) {

                return ResponseEntity.status(403)
                        .body(Map.of("error",
                                "Impossible d'enregistrer une transaction pour une autre agence"));
            }

            if (transaction.getAgenceReceptionId() != null &&
                !transaction.getAgenceReceptionId().equals(agenceUser)) {

                return ResponseEntity.status(403)
                        .body(Map.of("error",
                                "Impossible d'enregistrer une transaction pour une autre agence"));
            }
        }

        Transaction saved = service.creerTransaction(transaction);

        return ResponseEntity.ok(Map.of(
                "message", "Transaction enregistrée avec succès",
                "transaction", saved
        ));
    }

    /* ===================== LISTE ===================== */
    @GetMapping
    public ResponseEntity<?> all(Authentication auth) {

        Utilisateur current = getCurrentUser(auth);

        // SUPERADMIN -> tout voir
        if (current.getRole() == Role.SUPERADMIN) {
            return ResponseEntity.ok(service.getAll());
        }

        // AGENT -> uniquement ses propres transactions
        if (current.getRole() == Role.AGENT) {
            return ResponseEntity.ok(service.getByUser(current.getId()));
        }

        // ADMIN + RESPONSABLE -> toutes transactions de leur agence
        return ResponseEntity.ok(
                service.getByAgence(current.getAgence().getId())
        );
    }

    /* ===================== DETAILS ===================== */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id,
                                 Authentication auth) {

        Utilisateur current = getCurrentUser(auth);

        Transaction t = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));

        // Restriction d'accès selon rôle
        if (current.getRole() != Role.SUPERADMIN) {

            Integer agenceUser = current.getAgence().getId();

            boolean allowed =
                    (t.getAgenceEnvoiId() != null && t.getAgenceEnvoiId().equals(agenceUser)) ||
                    (t.getAgenceReceptionId() != null && t.getAgenceReceptionId().equals(agenceUser));

            // Agent : doit aussi être auteur
            if (current.getRole() == Role.AGENT) {
                allowed = allowed && t.getUtilisateurId().equals(current.getId());
            }

            if (!allowed)
                return ResponseEntity.status(403).body(Map.of("error", "Accès interdit"));
        }

        return ResponseEntity.ok(t);
    }

    /* ===================== SUPPRESSION ===================== */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    Authentication auth) {

        Utilisateur current = getCurrentUser(auth);

        Transaction t = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));

        // Agent ne supprime jamais
        if (current.getRole() == Role.AGENT) {
            return ResponseEntity.status(403).body(Map.of("error", "Suppression interdite"));
        }

        // Admin / Responsable -> seulement leur agence
        if (current.getRole() != Role.SUPERADMIN) {

            Integer agenceUser = current.getAgence().getId();

            boolean allowed =
                    (t.getAgenceEnvoiId() != null && t.getAgenceEnvoiId().equals(agenceUser)) ||
                    (t.getAgenceReceptionId() != null && t.getAgenceReceptionId().equals(agenceUser));

            if (!allowed)
                return ResponseEntity.status(403).body(Map.of("error", "Suppression interdite"));
        }

        service.supprimer(id);
        return ResponseEntity.ok(Map.of("message", "Supprimé"));
    }
}
