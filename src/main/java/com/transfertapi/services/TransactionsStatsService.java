package com.transfertapi.services;

import com.transfertapi.entities.StatutCodeTransfert;
import com.transfertapi.entities.Transaction;
import com.transfertapi.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionsStatsService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private AgenceRepository agenceRepository;
    @Autowired private CodeTransfertRepository codeTransfertRepository;

    // ================================
    //        UTILITAIRES DATE
    // ================================

    private LocalDateTime startToday() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime endToday() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }

    private double safeDouble(BigDecimal m) {
        return (m != null) ? m.doubleValue() : 0.0;
    }

    // ================================
    //        STATS PAR ROLE
    // ================================

    public Map<String, Object> getStatsGlobal() {
        return buildDataMap(
                transactionRepository.countBetween(startToday(), endToday()),
                safeDouble(transactionRepository.sumBetween(startToday(), endToday())),
                transactionRepository.findAllBetween(startToday(), endToday())
        );
    }

    public Map<String, Object> getStatsByAgence(Integer agenceId) {
        return buildDataMap(
                transactionRepository.countByAgenceBetween(agenceId, startToday(), endToday()),
                safeDouble(transactionRepository.sumByAgenceBetween(agenceId, startToday(), endToday())),
                transactionRepository.findAllByAgenceBetween(agenceId, startToday(), endToday())
        );
    }

    public Map<String, Object> getStatsByUtilisateur(Integer userId, Integer agenceId) {
        return buildDataMap(
                transactionRepository.countByUserBetween(userId, agenceId, startToday(), endToday()),
                safeDouble(transactionRepository.sumByUserBetween(userId, agenceId, startToday(), endToday())),
                transactionRepository.findAllByUserBetween(userId, agenceId, startToday(), endToday())
        );
    }

    // ================================
    //        CONSTRUCTION MAP
    // ================================

    /**
     * ⚠️ Cette méthode ne casse aucune clé existante.
     * Elle ajoute seulement des statistiques supplémentaires.
     */
    private Map<String, Object> buildDataMap(long count, double sum, List<Transaction> lines) {

        Map<String, Object> data = new HashMap<>();

        // ==========================================
        // 1️⃣ STATISTIQUES TRANSACTIONS (DU JOUR)
        // ==========================================

        data.put("nombreTransactions", count);
        data.put("totalTransactions", count);
        data.put("montantTotal", sum);
        data.put("volumeTotal", sum);
        data.put("lignes", lines);

        // ==========================================
        // 2️⃣ STATISTIQUES ENTREPRISE (GLOBAL)
        // ==========================================

        long totalClients = clientRepository.count();
        long totalAgences = agenceRepository.countByActifTrue();

        data.put("nombreClients", totalClients);
        data.put("totalClients", totalClients);
        data.put("nombreAgences", totalAgences);
        data.put("totalAgences", totalAgences);
        data.put("agencesActives", totalAgences);

        // ==========================================
        // 3️⃣ 🔥 NOUVELLES STATS : RETRAITS EN ATTENTE
        // ==========================================

        long retraitsEnAttente = 0L;
        double montantEnAttente = 0.0;

        try {
            retraitsEnAttente = codeTransfertRepository.countByStatut(
                    StatutCodeTransfert.ENVOYE
            );

            montantEnAttente = safeDouble(
                    codeTransfertRepository.sumByStatut(
                            StatutCodeTransfert.ENVOYE
                    )
            );

        } catch (Exception e) {
            // Sécurité : on ne casse jamais le dashboard
            retraitsEnAttente = 0L;
            montantEnAttente = 0.0;
        }

        data.put("retraitsEnAttente", retraitsEnAttente);
        data.put("montantEnAttente", montantEnAttente);

        return data;
    }
}