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

    /* === GLOBAL === */
    public TransactionsStatsDTO getStatsGlobal() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long transactions = transactionRepository.countBetween(start, end);
        BigDecimal montant = transactionRepository.sumBetween(start, end);
        if (montant == null) montant = BigDecimal.ZERO;

        long clients = clientRepository.count();
        long agences = agenceRepository.countByActifTrue();

        return new TransactionsStatsDTO(
                transactions,
                clients,
                agences,
                montant.doubleValue()
        );
    }

    /* === PAR AGENCE === */
    public TransactionsStatsDTO getStatsByAgence(Integer agenceId) {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long transactions =
                transactionRepository.countByAgenceBetween(agenceId, start, end);

        BigDecimal montant =
                transactionRepository.sumByAgenceBetween(agenceId, start, end);

        if (montant == null) montant = BigDecimal.ZERO;

        return new TransactionsStatsDTO(
                transactions,
                clientRepository.count(),
                1,
                montant.doubleValue()
        );
    }

    /* === PAR UTILISATEUR === */
    public TransactionsStatsDTO getStatsByUtilisateur(Integer userId, Integer agenceId) {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long transactions =
                transactionRepository.countByUserBetween(userId, agenceId, start, end);

        BigDecimal montant =
                transactionRepository.sumByUserBetween(userId, agenceId, start, end);

        if (montant == null) montant = BigDecimal.ZERO;

        return new TransactionsStatsDTO(
                transactions,
                clientRepository.count(),
                1,
                montant.doubleValue()
        );
    }
}
