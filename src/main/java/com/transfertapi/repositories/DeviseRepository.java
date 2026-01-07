package com.transfertapi.repositories;

import com.transfertapi.entities.Devise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviseRepository extends JpaRepository<Devise, Integer> {
    Optional<Devise> findByCode(String code);
    boolean existsByCode(String code);
}
