package com.transfertapi.repositories;

import com.transfertapi.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    // Recherche unique par téléphone
    Optional<Client> findByTelephone(String telephone);

    // Vérification d'unicité du téléphone
    boolean existsByTelephone(String telephone);

    // Recherche partielle par nom
    List<Client> findByNomContainingIgnoreCase(String nom);
}
