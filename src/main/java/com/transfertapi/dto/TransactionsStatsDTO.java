package com.transfertapi.dto;

public class TransactionsStatsDTO {

    private long totalTransactions;
    private long totalClients;
    private long totalAgences;
    private double montantTotal;

    public TransactionsStatsDTO() {}

    public TransactionsStatsDTO(long totalTransactions,
                                long totalClients,
                                long totalAgences,
                                double montantTotal) {
        this.totalTransactions = totalTransactions;
        this.totalClients = totalClients;
        this.totalAgences = totalAgences;
        this.montantTotal = montantTotal;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public long getTotalClients() {
        return totalClients;
    }

    public long getTotalAgences() {
        return totalAgences;
    }

    public double getMontantTotal() {
        return montantTotal;
    }
}
