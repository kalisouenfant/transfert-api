package com.transfertapi.repositories;

import com.transfertapi.entities.JournalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalActionRepository extends JpaRepository<JournalAction, Integer> {

    List<JournalAction> findByUtilisateurId(Integer utilisateurId);

    List<JournalAction> findByActionContainingIgnoreCase(String action);

    // Nouvelle méthode : Jointure entre JournalAction et Utilisateur pour filtrer par agence
    @Query("SELECT j FROM JournalAction j WHERE j.utilisateurId IN " +
           "(SELECT u.id FROM Utilisateur u WHERE u.agence.id = :agenceId)")
    List<JournalAction> findAllByAgenceId(@Param("agenceId") Integer agenceId);
}