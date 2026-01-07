package com.transfertapi.services;

import com.transfertapi.entities.PieceIdentite;
import com.transfertapi.repositories.PieceIdentiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PieceIdentiteService {

    @Autowired
    private PieceIdentiteRepository pieceIdentiteRepository;

    // 🔹 Lister toutes les pièces d’un client
    public List<PieceIdentite> getByClient(Integer clientId) {
        return pieceIdentiteRepository.findByClientId(clientId);
    }

    // 🔹 Récupérer une pièce par ID
    public Optional<PieceIdentite> getById(Integer id) {
        return pieceIdentiteRepository.findById(id);
    }

    // 🔹 Enregistrer ou modifier une pièce
    public PieceIdentite save(PieceIdentite piece) {
        return pieceIdentiteRepository.save(piece);
    }

    // 🔹 Supprimer une pièce
    public void delete(Integer id) {
        pieceIdentiteRepository.deleteById(id);
    }

    // 🔹 Vérifier si une pièce existe déjà
    public boolean existsByNumero(String numero) {
        return pieceIdentiteRepository.existsByNumero(numero);
    }
}
