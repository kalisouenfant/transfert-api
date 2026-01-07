package com.transfertapi.controllers;

import com.transfertapi.entities.*;
import com.transfertapi.services.CodeTransfertService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/codes-transfert")
@CrossOrigin(origins = "*")
public class CodeTransfertController {

    @Autowired
    private CodeTransfertService service;

    @Autowired
    private UtilisateurService utilisateurService;

    private Utilisateur getCurrentUser(Authentication auth) {
        return utilisateurService.getByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @GetMapping
    public ResponseEntity<Page<CodeTransfert>> list(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            Authentication auth
    ) {
        Utilisateur current = getCurrentUser(auth);

        String[] sortParts = sort.split(",");
        Sort sorting = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Integer agenceIdFilter = null;

        if (current.getRole() != Role.SUPERADMIN && current.getAgence() != null) {
            agenceIdFilter = current.getAgence().getId();
        }

        return ResponseEntity.ok(
                service.listFiltered(statut, search, agenceIdFilter, pageable)
        );
    }

    @PostMapping
    public ResponseEntity<?> creer(@RequestBody Map<String, Object> data,
                                   Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        data.put("utilisateurId", current.getId());
        return ResponseEntity.status(201).body(service.creer(data));
    }

    @PutMapping("/annuler/{code}")
    public ResponseEntity<?> annuler(@PathVariable String code, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        return ResponseEntity.ok(service.annuler(code, current));
    }

    @PutMapping("/retirer/{code}")
    public ResponseEntity<?> retirer(@PathVariable String code, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        Integer agenceId = current.getAgence().getId();

        return ResponseEntity.ok(
                service.retirer(code, current.getId(), agenceId)
        );
    }

    @GetMapping("/chercher/{code}")
    public ResponseEntity<?> verifier(@PathVariable String code) {
        return ResponseEntity.ok(service.verifier(code));
    }
}
