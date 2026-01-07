package com.transfertapi.controllers;

import com.transfertapi.entities.Notification;
import com.transfertapi.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 🔹 Lister toutes les notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    // 🔹 Récupérer une notification par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return notificationService.getById(id)
                .map(ResponseEntity::<Object>ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "❌ Notification introuvable")));
    }

    // 🔹 Récupérer les notifications d’un client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Notification>> getByClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(notificationService.getByClient(clientId));
    }

    // 🔹 Créer une nouvelle notification
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Notification notification) {
        Notification saved = notificationService.save(notification);
        return ResponseEntity.ok(Map.of(
                "message", "✅ Notification créée avec succès",
                "notification", saved
        ));
    }

    // 🔹 Marquer une notification comme envoyée
    @PutMapping("/envoyer/{id}")
    public ResponseEntity<?> marquerEnvoyee(@PathVariable Integer id) {
        Notification updated = notificationService.marquerCommeEnvoyee(id);
        if (updated != null) {
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Notification marquée comme envoyée",
                    "notification", updated
            ));
        }
        return ResponseEntity.status(404).body(Map.of("error", "❌ Notification introuvable"));
    }

    // 🔹 Supprimer une notification
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        notificationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "✅ Notification supprimée avec succès"));
    }
}
