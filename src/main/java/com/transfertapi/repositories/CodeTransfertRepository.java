package com.transfertapi.repositories;

import com.transfertapi.entities.CodeTransfert;
import com.transfertapi.entities.StatutCodeTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CodeTransfertRepository extends JpaRepository<CodeTransfert, Integer> {

    Optional<CodeTransfert> findByCode(String code);

    // Recherche globale pour les Super-Admins
    @Query("SELECT c FROM CodeTransfert c WHERE " +
           "(:statut IS NULL OR c.statut = :statut) AND " +
           "(:search IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CodeTransfert> searchGlobal(
            @Param("statut") StatutCodeTransfert statut,
            @Param("search") String search,
            Pageable pageable
    );

    // Recherche restreinte à une agence (Envoi ou Réception)
    @Query("SELECT c FROM CodeTransfert c WHERE " +
           "(:statut IS NULL OR c.statut = :statut) AND " +
           "(c.agenceEnvoiId = :agenceId OR c.agenceReceptionId = :agenceId) AND " +
           "(:search IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CodeTransfert> searchByAgence(
            @Param("statut") StatutCodeTransfert statut,
            @Param("search") String search,
            @Param("agenceId") Integer agenceId,
            Pageable pageable
    );

    // Vérifie si l'agence est liée à des transferts
    boolean existsByAgenceEnvoiIdOrAgenceReceptionId(Integer agenceEnvoiId, Integer agenceReceptionId);

    /* =====================================================
       🔥 AJOUT POUR LE DASHBOARD (NE CASSE RIEN)
       ===================================================== */

    // Nombre de codes par statut (ex: ENVOYE = retraits en attente)
    long countByStatut(StatutCodeTransfert statut);

    // Somme des montants par statut
    @Query("SELECT COALESCE(SUM(c.montant), 0) FROM CodeTransfert c WHERE c.statut = :statut")
    BigDecimal sumByStatut(@Param("statut") StatutCodeTransfert statut);
}