package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction type;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatutTransaction statut;

    private Integer codeTransfertId;

    private Integer expediteurClientId;
    private Integer beneficiaireClientId;

    private Integer agenceEnvoiId;
    private Integer agenceReceptionId;

    private Integer utilisateurId;

    private Integer transactionParenteId;

    @Column(nullable=false)
    private LocalDateTime dateTransaction = LocalDateTime.now();
}
