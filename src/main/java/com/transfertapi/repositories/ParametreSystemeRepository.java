package com.transfertapi.repositories;

import com.transfertapi.entities.ParametreSysteme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametreSystemeRepository extends JpaRepository<ParametreSysteme, Integer> {
    Optional<ParametreSysteme> findByCle(String cle);
    boolean existsByCle(String cle);
}
