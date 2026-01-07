package com.transfertapi.controllers;

import com.transfertapi.dto.TransactionsStatsDTO;
import com.transfertapi.entities.Role;
import com.transfertapi.entities.Utilisateur;
import com.transfertapi.services.TransactionsStatsService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionsStatsController {

    @Autowired
    private TransactionsStatsService statsService;

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/stats")
    public ResponseEntity<TransactionsStatsDTO> getStats(Authentication auth) {

        Utilisateur user = utilisateurService
                .getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // SUPERADMIN → stats globales
        if (user.getRole() == Role.SUPERADMIN) {
            return ResponseEntity.ok(statsService.getStatsGlobal());
        }

        // RESPONSABLE → stats de son agence
        if (user.getRole() == Role.RESPONSABLE) {
            return ResponseEntity.ok(
                    statsService.getStatsByAgence(user.getAgence().getId())
            );
        }

        // AGENT → uniquement ses opérations dans son agence
        return ResponseEntity.ok(
                statsService.getStatsByUtilisateur(
                        user.getId(),
                        user.getAgence().getId()
                )
        );
    }
}
