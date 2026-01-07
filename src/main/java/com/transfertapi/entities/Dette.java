package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dettes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Dette {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne @JoinColumn(name = "agence_id")
    private Agence agence;

    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private StatutDette statut = StatutDette.NON_PAYEE;

    private LocalDateTime dateEmission;
    private LocalDateTime dateReglement;
}
