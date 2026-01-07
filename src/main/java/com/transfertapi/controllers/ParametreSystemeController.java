package com.transfertapi.controllers;

import com.transfertapi.entities.ParametreSysteme;
import com.transfertapi.services.ParametreSystemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parametres")
@CrossOrigin(origins = "*")
public class ParametreSystemeController {

    @Autowired
    private ParametreSystemeService parametreSystemeService;

    // 🔹 Lister tous les paramètres
    @GetMapping
    public ResponseEntity<List<ParametreSysteme>> getAll() {
        return ResponseEntity.ok(parametreSystemeService.getAll());
    }

    // 🔹 Récupérer un paramètre par clé
    @GetMapping("/{cle}")
    public ResponseEntity<?> getByCle(@PathVariable String cle) {
        return parametreSystemeService.getByCle(cle)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "❌ Paramètre introuvable")));
    }

    // 🔹 Créer un nouveau paramètre
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ParametreSysteme parametre) {
        if (parametreSystemeService.existsByCle(parametre.getCle())) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ Cette clé existe déjà."));
        }
        ParametreSysteme saved = parametreSystemeService.save(parametre);
        return ResponseEntity.ok(Map.of(
                "message", "✅ Paramètre ajouté avec succès",
                "parametre", saved
        ));
    }

    // 🔹 Mettre à jour un paramètre
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody ParametreSysteme parametre) {
        parametre.setId(id);
        ParametreSysteme updated = parametreSystemeService.save(parametre);
        return ResponseEntity.ok(Map.of(
                "message", "✅ Paramètre mis à jour",
                "parametre", updated
        ));
    }

    // 🔹 Supprimer un paramètre
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        parametreSystemeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "✅ Paramètre supprimé avec succès"));
    }
}
