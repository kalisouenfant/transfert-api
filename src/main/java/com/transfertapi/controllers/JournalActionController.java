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
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Utilisateur non trouvé")
                );
    }

    /** Lister tout — Admin & SuperAdmin uniquement */
    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {

        Utilisateur u = current(auth);

        if (u.getRole() == Role.SUPERADMIN || u.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(service.getAll());
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Accès réservé à l'administration"
        );
    }

    /** Par ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Action introuvable"));
    }

    /** Historique d’un utilisateur */
    @GetMapping("/utilisateur/{id}")
    public ResponseEntity<List<JournalAction>> getByUtilisateur(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getByUtilisateur(id));
    }

    /** Enregistrer */
    @PostMapping("/enregistrer")
    public ResponseEntity<?> enregistrer(@RequestBody JournalAction action) {

        if (action.getUtilisateurId() == null || action.getAction() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Utilisateur et action obligatoires"
            );
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
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Suppression réservée au SuperAdmin"
            );
        }

        service.delete(id);

        return ResponseEntity.ok(Map.of("message", "Supprimé"));
    }
}
