package com.transfertapi.repositories;

import com.transfertapi.entities.MouvementCaisse;
import com.transfertapi.entities.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MouvementCaisseRepository extends JpaRepository<MouvementCaisse, Integer>, JpaSpecificationExecutor<MouvementCaisse> {

    List<MouvementCaisse> findByAgence(Agence agence);

    // Calcul du solde sécurisé avec COALESCE pour éviter les valeurs NULL
    @Query("SELECT COALESCE(SUM(CASE WHEN m.type = 'ENTREE' THEN m.montant ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN m.type = 'SORTIE' THEN m.montant ELSE 0 END), 0) " +
           "FROM MouvementCaisse m " +
           "WHERE m.agence.id = :agenceId")
    BigDecimal calculerSoldeAgence(@Param("agenceId") Integer agenceId);

    // FIX : Pour AgenceService (Vérifie si l'agence a un historique financier)
    boolean existsByAgenceId(Integer agenceId);
}