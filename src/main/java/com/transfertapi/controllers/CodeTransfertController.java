package com.transfertapi.controllers;

import com.transfertapi.entities.*;
import com.transfertapi.services.CodeTransfertService;
import com.transfertapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    // --- 0. LISTE (Nouveau : pour corriger l'erreur GET) ---
    @GetMapping
    public ResponseEntity<Page<CodeTransfert>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) Integer agenceId
    ) {
        if (agenceId != null) {
            return ResponseEntity.ok(service.getTransfertsByAgence(agenceId, page, size));
        }
        return ResponseEntity.ok(service.getAllTransferts(page, size));
    }

    // --- 1. ENVOI ---
    @PostMapping
    public ResponseEntity<CodeTransfert> creer(@RequestBody Map<String, Object> data, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        return ResponseEntity.status(201).body(service.creer(data, current));
    }

    // --- 2. RETRAIT ---
    @PutMapping("/retirer/{code}")
    public ResponseEntity<CodeTransfert> retirer(@PathVariable String code, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        return ResponseEntity.ok(service.retirer(code, current));
    }

    // --- 3. ANNULATION ---
    @PutMapping("/annuler/{code}")
    public ResponseEntity<CodeTransfert> annuler(@PathVariable String code, Authentication auth) {
        Utilisateur current = getCurrentUser(auth);
        return ResponseEntity.ok(service.annuler(code, current));
    }

    // --- CONSULTATION ---
    @GetMapping("/chercher/{code}")
    public ResponseEntity<CodeTransfert> verifier(@PathVariable String code) {
        return ResponseEntity.ok(service.verifier(code));
    }
}