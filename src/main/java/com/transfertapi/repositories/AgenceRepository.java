package com.transfertapi.repositories;

import com.transfertapi.entities.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgenceRepository extends JpaRepository<Agence, Integer> {

    Optional<Agence> findByNom(String nom);

    boolean existsByNom(String nom);

    long countByActifTrue();
}
