package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "codes_transfert")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CodeTransfert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 12)
    private String code;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCodeTransfert statut = StatutCodeTransfert.ENVOYE;

    @Column(nullable = false)
    private Integer expediteurClientId;

    @Column(nullable = false)
    private Integer beneficiaireClientId;

    @Column(nullable = false)
    private Integer agenceEnvoiId;

    @Column
    private Integer agenceReceptionId;

    @Column(nullable = false)
    private Integer utilisateurId;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @Column
    private LocalDateTime dateRetrait;
    
    // ===========================================
    // NOUVEAUX CHAMPS POUR L'ANNULATION (CORRECTION)
    // ===========================================

    /** Date et heure de l'annulation du code de transfert. */
    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

    /** ID de l'utilisateur (employé) qui a effectué l'annulation. */
    @Column(name = "utilisateur_annulation_id")
    private Integer utilisateurAnnulationId;
    
    // ===========================================
    // FIN DES AJOUTS
    // ===========================================
}