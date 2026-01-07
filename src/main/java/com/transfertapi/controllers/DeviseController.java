package com.transfertapi.controllers;

import com.transfertapi.entities.Devise;
import com.transfertapi.services.DeviseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devises")
@CrossOrigin(origins = "*")
public class DeviseController {

    @Autowired
    private DeviseService deviseService;

    // 🔹 Lister toutes les devises
    @GetMapping("/liste")
    public ResponseEntity<List<Devise>> getAll() {
        return ResponseEntity.ok(deviseService.getAll());
    }

    // 🔹 Rechercher une devise par code
    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        return deviseService.getByCode(code)
                .<ResponseEntity<?>>map(devise -> ResponseEntity.ok().body(devise))
                .orElseGet(() -> ResponseEntity
                        .status(404)
                        .body(Map.of("error", "❌ Devise introuvable")));
    }

    // 🔹 Créer une nouvelle devise
    @PostMapping("/creer")
    public ResponseEntity<?> create(@RequestBody Devise devise) {
        if (deviseService.existsByCode(devise.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ Ce code de devise existe déjà !"));
        }

        Devise saved = deviseService.save(devise);
        return ResponseEntity.ok(Map.of(
                "message", "✅ Devise créée avec succès",
                "devise", saved
        ));
    }

    // 🔹 Mettre à jour une devise
    @PutMapping("/modifier/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Devise devise) {
        devise.setId(id);
        Devise updated = deviseService.save(devise);
        return ResponseEntity.ok(Map.of(
                "message", "✅ Devise mise à jour avec succès",
                "devise", updated
        ));
    }

    // 🔹 Supprimer une devise
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        deviseService.delete(id);
        return ResponseEntity.ok(Map.of("message", "✅ Devise supprimée avec succès"));
    }
}
