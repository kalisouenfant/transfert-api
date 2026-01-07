package com.transfertapi.repositories;

import com.transfertapi.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    /* ========= GLOBAL ========= */
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.dateTransaction >= :start AND t.dateTransaction < :end")
    long countBetween(@Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.montant), 0) FROM Transaction t " +
           "WHERE t.dateTransaction >= :start AND t.dateTransaction < :end")
    BigDecimal sumBetween(@Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);


    /* ========= PAR AGENCE ========= */
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE (t.agenceEnvoiId = :agence OR t.agenceReceptionId = :agence) " +
           "AND t.dateTransaction >= :start AND t.dateTransaction < :end")
    long countByAgenceBetween(@Param("agence") Integer agence,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.montant),0) FROM Transaction t " +
           "WHERE (t.agenceEnvoiId = :agence OR t.agenceReceptionId = :agence) " +
           "AND t.dateTransaction >= :start AND t.dateTransaction < :end")
    BigDecimal sumByAgenceBetween(@Param("agence") Integer agence,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    List<Transaction> findByAgenceEnvoiIdOrAgenceReceptionId(Integer agenceEnvoiId,
                                                             Integer agenceReceptionId);


    /* ========= PAR UTILISATEUR ========= */
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.utilisateurId = :user AND t.agenceEnvoiId = :agence " +
           "AND t.dateTransaction >= :start AND t.dateTransaction < :end")
    long countByUserBetween(@Param("user") Integer user,
                            @Param("agence") Integer agence,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.montant),0) FROM Transaction t " +
           "WHERE t.utilisateurId = :user AND t.agenceEnvoiId = :agence " +
           "AND t.dateTransaction >= :start AND t.dateTransaction < :end")
    BigDecimal sumByUserBetween(@Param("user") Integer user,
                                @Param("agence") Integer agence,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    List<Transaction> findByUtilisateurId(Integer userId);
}
