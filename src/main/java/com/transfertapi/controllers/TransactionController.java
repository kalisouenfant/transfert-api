package com.transfertapi.controllers;

import com.transfertapi.entities.Role;
import com.transfertapi.entities.Transaction;
import com.transfertapi.entities.Utilisateur;
import com.transfertapi.exceptions.ResourceNotFoundException;
import com.transfertapi.services.TransactionService;
import com.transfertapi.services.TransactionsStatsService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService service;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private TransactionsStatsService statsService;

    /**
     * Helper pour récupérer l'utilisateur connecté via le token JWT
     */
    private Utilisateur getCurrentUser(Authentication auth) {
        return utilisateurService.getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /* ===================== STATISTIQUES & RAPPORTS ===================== */
    
    /**
     * Cette méthode centralise les statistiques pour le Dashboard et les Rapports.
     * Elle supporte plusieurs URLs pour assurer la compatibilité avec le client NetBeans.
     */
    @GetMapping({"/transactions/stats", "/rapports/global", "/rapports/par-agence", "/rapports/par-utilisateur"})
    public ResponseEntity<?> getStats(Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        try {
            // Logique de filtrage par rôle déléguée au StatsService
            if (current.getRole() == Role.SUPERADMIN || current.getRole() == Role.ADMIN) {
                return ResponseEntity.ok(statsService.getStatsGlobal());
            } 
            
            if (current.getRole() == Role.RESPONSABLE) {
                Integer agenceId = current.getAgence().getId();
                return ResponseEntity.ok(statsService.getStatsByAgence(agenceId));
            }
            
            if (current.getRole() == Role.AGENT) {
                Integer userId = current.getId();
                Integer agenceId = current.getAgence().getId();
                return ResponseEntity.ok(statsService.getStatsByUtilisateur(userId, agenceId));
            }
            
            return ResponseEntity.status(403).body(Map.of("error", "Rôle non autorisé"));
            
        } catch (Exception e) {
            // Fallback : renvoie une structure JSON vide mais valide pour éviter le crash du client Swing
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("totalTransactions", 0L);
            errorMap.put("nombreTransactions", 0L);
            errorMap.put("volumeTotal", 0.0);
            errorMap.put("montantTotal", 0.0);
            errorMap.put("totalClients", 0L);
            errorMap.put("totalAgences", 0L);
            errorMap.put("lignes", new ArrayList<>());
            return ResponseEntity.ok(errorMap);
        }
    }

    /* ===================== CRUD TRANSACTIONS (PRÉSERVÉ) ===================== */

    @PostMapping("/transactions")
    public ResponseEntity<?> create(@RequestBody Transaction transaction, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        
        // Sécurité : Un non-superadmin ne peut pas enregistrer pour une autre agence
        if (current.getRole() != Role.SUPERADMIN) {
            Integer agenceUser = current.getAgence().getId();
            if (transaction.getAgenceEnvoiId() != null && !transaction.getAgenceEnvoiId().equals(agenceUser)) {
                return ResponseEntity.status(403).body(Map.of("error", "Interdit d'envoyer pour une autre agence"));
            }
        }
        
        Transaction saved = service.creerTransaction(transaction);
        return ResponseEntity.ok(Map.of(
            "message", "Transaction enregistrée avec succès", 
            "transaction", saved
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> all(Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        
        if (current.getRole() == Role.SUPERADMIN || current.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(service.getAll());
        }
        if (current.getRole() == Role.AGENT) {
            return ResponseEntity.ok(service.getByUser(current.getId()));
        }
        // Pour les responsables : transactions de leur agence
        return ResponseEntity.ok(service.getByAgence(current.getAgence().getId()));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        Transaction t = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));
        
        // Vérification des droits d'accès granulaire
        if (current.getRole() != Role.SUPERADMIN && current.getRole() != Role.ADMIN) {
            Integer agenceUser = current.getAgence().getId();
            boolean isLienAgence = (t.getAgenceEnvoiId() != null && t.getAgenceEnvoiId().equals(agenceUser)) ||
                                  (t.getAgenceReceptionId() != null && t.getAgenceReceptionId().equals(agenceUser));
            
            if (!isLienAgence) {
                return ResponseEntity.status(403).body(Map.of("error", "Accès interdit : agence différente"));
            }
            
            if (current.getRole() == Role.AGENT && !t.getUtilisateurId().equals(current.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Accès interdit : vous n'êtes pas l'auteur"));
            }
        }
        return ResponseEntity.ok(t);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        Transaction t = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));
        
        if (current.getRole() == Role.AGENT) {
            return ResponseEntity.status(403).body(Map.of("error", "Suppression interdite pour les agents"));
        }
        
        if (current.getRole() != Role.SUPERADMIN) {
            Integer agenceUser = current.getAgence().getId();
            boolean isLienAgence = (t.getAgenceEnvoiId() != null && t.getAgenceEnvoiId().equals(agenceUser)) ||
                                  (t.getAgenceReceptionId() != null && t.getAgenceReceptionId().equals(agenceUser));
            
            if (!isLienAgence) {
                return ResponseEntity.status(403).body(Map.of("error", "Suppression interdite pour une transaction hors agence"));
            }
        }
        
        service.supprimer(id);
        return ResponseEntity.ok(Map.of("message", "Transaction supprimée avec succès"));
    }
}