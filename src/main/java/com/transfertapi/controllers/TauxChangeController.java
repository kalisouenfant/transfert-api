package com.transfertapi.controllers;

import com.transfertapi.entities.TauxChange;
import com.transfertapi.services.TauxChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // Import nécessaire
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taux")
@CrossOrigin(origins = "*")
public class TauxChangeController {

    @Autowired
    private TauxChangeService tauxChangeService;

    // 🔹 Lister tous les taux (GET /api/taux)
    @GetMapping
    public ResponseEntity<List<TauxChange>> getAll() {
        return ResponseEntity.ok(tauxChangeService.getAll());
    }

    // 🔹 Obtenir un taux par ID (GET /api/taux/{id})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return tauxChangeService.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "❌ Taux introuvable")));
    }
    
    /**
     * NOUVEAU : Obtenir le taux le plus récent pour une paire spécifique.
     * GET /api/taux/latest?source=XOF&cible=LRD
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestTaux(
            @RequestParam String source, 
            @RequestParam String cible) {
        return tauxChangeService.getLatestTauxChange(source, cible)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "❌ Taux récent non trouvé pour " + source + "/" + cible)));
    }


    // 🔹 Créer un taux (POST /api/taux)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TauxChange taux) {
        try {
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Taux enregistré avec succès",
                    "taux", tauxChangeService.save(taux)
            ));
        } catch (IllegalArgumentException e) {
             // Erreur de validation levée par le service (taux nul, devise inconnue, etc.)
             return ResponseEntity.badRequest().body(Map.of("error", "⚠️ Erreur de validation: " + e.getMessage()));
        } catch (Exception e) {
             return ResponseEntity.internalServerError().body(Map.of("error", "❌ Erreur serveur lors de la création : " + e.getMessage()));
        }
    }

    // 🔹 Mettre à jour un taux (PUT /api/taux/{id})
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TauxChange taux) {
        try {
            return tauxChangeService.getById(id)
                    .<ResponseEntity<?>>map(existing -> {
                        taux.setId(id);
                        return ResponseEntity.ok(Map.of(
                                "message", "✅ Taux mis à jour",
                                "taux", tauxChangeService.save(taux)
                        ));
                    })
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "❌ Taux non trouvé")));
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(Map.of("error", "⚠️ Erreur de validation: " + e.getMessage()));
        } catch (Exception e) {
             return ResponseEntity.internalServerError().body(Map.of("error", "❌ Erreur serveur lors de la mise à jour : " + e.getMessage()));
        }
    }

    // 🔹 Supprimer un taux (DELETE /api/taux/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            tauxChangeService.delete(id);
            return ResponseEntity.ok(Map.of("message", "✅ Taux supprimé avec succès"));
        } catch (Exception e) {
             // Si l'ID n'existe pas, l'opération de suppression peut échouer selon la configuration JPA/DB
             return ResponseEntity.status(404).body(Map.of("error", "❌ Taux introuvable ou erreur lors de la suppression."));
        }
    }
    
    /**
     * NOUVEAU: Endpoint de conversion :
     * GET /api/taux/convertir?montant=10000&source=XOF&cible=LRD
     */
    @GetMapping("/convertir")
    public ResponseEntity<?> convertir(
            @RequestParam BigDecimal montant,
            @RequestParam String source,
            @RequestParam String cible) {
        try {
            BigDecimal montantConverti = tauxChangeService.convertir(montant, source, cible);
            return ResponseEntity.ok(Map.of(
                    "montantSource", montant,
                    "deviseSource", source,
                    "montantConverti", montantConverti,
                    "deviseCible", cible
            ));
        } catch (IllegalStateException e) {
            // Taux non trouvé (exception levée par le service)
            return ResponseEntity.status(404).body(Map.of("error", "❌ " + e.getMessage()));
        } catch (Exception e) {
            // Autres erreurs (montant non numérique, etc.)
            return ResponseEntity.internalServerError().body(Map.of("error", "❌ Erreur de conversion : " + e.getMessage()));
        }
    }
}