package com.transfertapi.repositories;

import com.transfertapi.entities.Dette;
import com.transfertapi.entities.StatutDette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetteRepository extends JpaRepository<Dette, Integer> {
    List<Dette> findByClientId(Integer clientId);
    List<Dette> findByAgenceId(Integer agenceId);
    List<Dette> findByStatut(StatutDette statut);
}
