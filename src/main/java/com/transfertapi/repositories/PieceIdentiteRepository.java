package com.transfertapi.repositories;

import com.transfertapi.entities.PieceIdentite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceIdentiteRepository extends JpaRepository<PieceIdentite, Integer> {
    List<PieceIdentite> findByClientId(Integer clientId);
    boolean existsByNumero(String numero);
}
