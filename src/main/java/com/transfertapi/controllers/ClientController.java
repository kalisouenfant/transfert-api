package com.transfertapi.controllers;

import com.transfertapi.entities.Client;
import com.transfertapi.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // -------------------- TOUS LES CLIENTS --------------------
    @GetMapping
    public ResponseEntity<List<Client>> getAll() {
        return ResponseEntity.ok(clientService.getAll());
    }

    // -------------------- CLIENT PAR ID --------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return clientService.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404)
                        .body(Map.of("error", "Client introuvable")));
    }

    // -------------------- NOUVEAU : CLIENT PAR TÉLÉPHONE --------------------
    @GetMapping("/telephone/{telephone}")
    public ResponseEntity<?> getByTelephone(@PathVariable String telephone) {
        // ✅ CORRECTION : Renvoyer une Map simple plutôt que l'Entité complète
        // Ceci garantit un format JSON propre attendu par le client Swing (clé "nom").
        return clientService.getByTelephone(telephone)
                .<ResponseEntity<?>>map(client -> ResponseEntity.ok(Map.of(
                        "id", client.getId(),
                        "nom", client.getNom(),
                        "telephone", client.getTelephone()
                )))
                .orElse(ResponseEntity.status(404)
                        .body(Map.of("error", "Aucun client trouvé pour ce téléphone")));
    }

    // -------------------- CRÉATION CLIENT --------------------
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Client client) {

        if (client.getTelephone() == null || client.getTelephone().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le téléphone est obligatoire"));
        }

        if (clientService.existsByTelephone(client.getTelephone())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Téléphone déjà utilisé"));
        }

        Client saved = clientService.save(client);
        return ResponseEntity.ok(Map.of("message", "Client créé avec succès", "client", saved));
    }

    // -------------------- MODIFICATION CLIENT --------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Client client) {
        return clientService.getById(id)
                .<ResponseEntity<?>>map(existing -> {

                    // Empêcher de changer vers un numéro déjà utilisé par un autre client
                    if (!client.getTelephone().equals(existing.getTelephone()) &&
                            clientService.existsByTelephone(client.getTelephone())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Ce téléphone est déjà utilisé par un autre client"));
                    }

                    client.setId(id);
                    Client updated = clientService.save(client);
                    return ResponseEntity.ok(Map.of("message", "Client mis à jour", "client", updated));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Client introuvable")));
    }

    // -------------------- SUPPRESSION CLIENT --------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        clientService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Client supprimé"));
    }
}