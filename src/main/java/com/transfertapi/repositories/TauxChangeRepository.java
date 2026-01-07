package com.transfertapi.repositories;

import com.transfertapi.entities.TauxChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TauxChangeRepository extends JpaRepository<TauxChange, Integer> {
    
    // Votre méthode existante pour la recherche de paires
    // NOTE: Elle peut retourner plusieurs résultats s'il y a des dates différentes.
    Optional<TauxChange> findByDeviseSourceCodeAndDeviseCibleCode(String source, String cible);

    /**
     * NOUVEAU: Trouve le taux de change le plus récent pour une paire de devises donnée.
     * @param sourceCode Code de la devise source (ex: "XOF").
     * @param cibleCode Code de la devise cible (ex: "LRD").
     * @return Le TauxChange le plus récent (celui avec la 'dateApplication' la plus récente).
     */
    @Query("SELECT t FROM TauxChange t " +
            "WHERE t.deviseSource.code = :sourceCode AND t.deviseCible.code = :cibleCode " +
            "ORDER BY t.dateApplication DESC") // Tri par date descendante
    Optional<TauxChange> findLatestRate(@Param("sourceCode") String sourceCode, @Param("cibleCode") String cibleCode);
}