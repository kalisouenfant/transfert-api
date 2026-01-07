package com.transfertapi.repositories;

import com.transfertapi.entities.JournalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalActionRepository extends JpaRepository<JournalAction, Integer> {

    List<JournalAction> findByUtilisateurId(Integer utilisateurId);

    List<JournalAction> findByActionContainingIgnoreCase(String action);
}
