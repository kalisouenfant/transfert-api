package com.transfertapi.controllers;

import com.transfertapi.entities.PieceIdentite;
import com.transfertapi.services.PieceIdentiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pieces-identite")
@CrossOrigin(origins = "*")
public class PieceIdentiteController {

    @Autowired
    private PieceIdentiteService pieceIdentiteService;

    // 🔹 Lister toutes les pièces d’un client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getByClient(@PathVariable Integer clientId) {
        List<PieceIdentite> pieces = pieceIdentiteService.getByClient(clientId);
        if (pieces.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "❌ Aucune pièce d'identité trouvée pour ce client"));
        }
        return ResponseEntity.ok(pieces);
    }

    // 🔹 Récupérer une pièce par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return pieceIdentiteService.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "❌ Pièce d'identité introuvable")));
    }

    // 🔹 Créer une nouvelle pièce
    @PostMapping("/creer")
    public ResponseEntity<?> create(@RequestBody PieceIdentite piece) {
        if (pieceIdentiteService.existsByNumero(piece.getNumero())) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ Ce numéro de pièce existe déjà !"));
        }

        PieceIdentite saved = pieceIdentiteService.save(piece);
        return ResponseEntity.ok(Map.of(
                "message", "✅ Pièce d'identité enregistrée avec succès",
                "piece", saved
        ));
    }

    // 🔹 Modifier une pièce existante
    @PutMapping("/modifier/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody PieceIdentite piece) {
        return pieceIdentiteService.getById(id)
                .map(existing -> {
                    piece.setId(id);
                    PieceIdentite updated = pieceIdentiteService.save(piece);
                    return ResponseEntity.ok(Map.of(
                            "message", "✅ Pièce d'identité mise à jour avec succès",
                            "piece", updated
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "❌ Pièce d'identité introuvable")));
    }

    // 🔹 Supprimer une pièce
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        pieceIdentiteService.delete(id);
        return ResponseEntity.ok(Map.of("message", "✅ Pièce d'identité supprimée avec succès"));
    }
}
