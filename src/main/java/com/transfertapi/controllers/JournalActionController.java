package com.transfertapi.controllers;

import com.transfertapi.entities.JournalAction;
import com.transfertapi.entities.Role;
import com.transfertapi.entities.Utilisateur;
import com.transfertapi.services.JournalActionService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/journal")
@CrossOrigin(origins = "*")
public class JournalActionController {

    private final JournalActionService service;
    private final UtilisateurService utilisateurService;

    public JournalActionController(JournalActionService service,
                                   UtilisateurService utilisateurService) {
        this.service = service;
        this.utilisateurService = utilisateurService;
    }

    private Utilisateur current(Authentication auth) {
        if (auth == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");

        return utilisateurService.getByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));
    }

    /** * Lister les actions du journal avec filtrage par rôle
     */
    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {
        Utilisateur u = current(auth);

        // 1. SUPERADMIN et ADMIN voient tout le journal
        if (u.getRole() == Role.SUPERADMIN || u.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(service.getAll());
        }

        // 2. RESPONSABLE voit uniquement les actions des utilisateurs de son agence
        if (u.getRole() == Role.RESPONSABLE) {
            if (u.getAgence() == null) {
                return ResponseEntity.ok(List.of()); // Aucune agence rattachée
            }

            Integer agenceId = u.getAgence().getId();
            
            // On récupère tout et on filtre (ou on appelle une méthode optimisée dans le service)
            List<JournalAction> journalAgence = service.getAll().stream()
                .filter(action -> {
                    // On vérifie si l'utilisateur qui a fait l'action appartient à la même agence
                    return utilisateurService.getById(action.getUtilisateurId())
                        .map(userAction -> userAction.getAgence() != null && userAction.getAgence().getId().equals(agenceId))
                        .orElse(false);
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(journalAgence);
        }

        // 3. AGENT n'a pas accès au journal
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Accés réservé à l'administration"
        );
    }

    /** Par ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id, Authentication auth) {
        Utilisateur u = current(auth);
        JournalAction action = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable"));

        // Vérification de sécurité pour le Responsable
        if (u.getRole() == Role.RESPONSABLE) {
            Utilisateur auteurAction = utilisateurService.getById(action.getUtilisateurId()).orElse(null);
            if (auteurAction == null || auteurAction.getAgence() == null || !auteurAction.getAgence().getId().equals(u.getAgence().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette action n'appartient pas à votre agence");
            }
        }

        return ResponseEntity.ok(action);
    }

    /** Historique d’un utilisateur spécifique */
    @GetMapping("/utilisateur/{id}")
    public ResponseEntity<List<JournalAction>> getByUtilisateur(@PathVariable Integer id, Authentication auth) {
        Utilisateur u = current(auth);
        
        // Un responsable ne peut voir que les utilisateurs de son agence
        if (u.getRole() == Role.RESPONSABLE) {
            Utilisateur cible = utilisateurService.getById(id).orElse(null);
            if (cible == null || cible.getAgence() == null || !cible.getAgence().getId().equals(u.getAgence().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cet utilisateur n'est pas dans votre agence");
            }
        }
        
        return ResponseEntity.ok(service.getByUtilisateur(id));
    }

    /** Enregistrer une action */
    @PostMapping("/enregistrer")
    public ResponseEntity<?> enregistrer(@RequestBody JournalAction action) {
        if (action.getUtilisateurId() == null || action.getAction() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur et action obligatoires");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Action enregistrée",
                        "journal", service.enregistrer(action)
                ));
    }

    /** Supprimer — seulement SuperAdmin */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, Authentication auth) {
        Utilisateur u = current(auth);
        if (u.getRole() != Role.SUPERADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Suppression réservée au SuperAdmin");
        }
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Supprimé"));
    }
}