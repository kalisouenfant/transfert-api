package com.transfertapi.services;

import com.transfertapi.entities.Client;
import com.transfertapi.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // Tous les clients
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    // Client par ID
    public Optional<Client> getById(Integer id) {
        return clientRepository.findById(id);
    }

    // Client par téléphone (champ unique)
    public Optional<Client> getByTelephone(String telephone) {
        return clientRepository.findByTelephone(telephone);
    }

    // Enregistrer ou mettre à jour
    public Client save(Client client) {
        client.setLastUpdate(LocalDateTime.now());
        return clientRepository.save(client);
    }

    // Supprimer
    public void delete(Integer id) {
        clientRepository.deleteById(id);
    }

    // Vérifier si téléphone existe
    public boolean existsByTelephone(String telephone) {
        return clientRepository.existsByTelephone(telephone);
    }

    // Recherche par nom (utile pour filtrage)
    public List<Client> searchByNom(String nom) {
        return clientRepository.findByNomContainingIgnoreCase(nom);
    }
}
