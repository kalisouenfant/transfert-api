package com.transfertapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionsStatsDTO {

    private long total;            // Total transactions du jour
    private long clients;          // Nombre total de clients
    private long agences;          // Nombre d’agences actives
    private double montantTotal;   // Montant total du jour
}
