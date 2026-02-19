package com.transfertapi.services;

import com.transfertapi.dto.TransactionsStatsDTO;
import com.transfertapi.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TransactionsStatsService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    private LocalDateTime startToday() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime endToday() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }

    /* ===== SUPERADMIN ===== */
    public TransactionsStatsDTO getStatsGlobal() {

        BigDecimal montant = transactionRepository.sumBetween(startToday(), endToday());

        return new TransactionsStatsDTO(
                transactionRepository.countBetween(startToday(), endToday()),
                clientRepository.count(),
                agenceRepository.countByActifTrue(),
                montant.doubleValue()
        );
    }

    /* ===== RESPONSABLE ===== */
    public TransactionsStatsDTO getStatsByAgence(Integer agenceId) {

        BigDecimal montant =
                transactionRepository.sumByAgenceBetween(agenceId, startToday(), endToday());

        return new TransactionsStatsDTO(
                transactionRepository.countByAgenceBetween(agenceId, startToday(), endToday()),
                clientRepository.count(),
                1,
                montant.doubleValue()
        );
    }

    /* ===== AGENT ===== */
    public TransactionsStatsDTO getStatsByUtilisateur(Integer userId, Integer agenceId) {

        BigDecimal montant =
                transactionRepository.sumByUserBetween(userId, agenceId, startToday(), endToday());

        return new TransactionsStatsDTO(
                transactionRepository.countByUserBetween(userId, agenceId, startToday(), endToday()),
                clientRepository.count(),
                1,
                montant.doubleValue()
        );
    }
}
