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

    @Column(nullable = false, unique = true, length = 15)
    private String code;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCodeTransfert statut;

    @Column(nullable = false)
    private Integer expediteurClientId;

    @Column(nullable = false)
    private Integer beneficiaireClientId;

    @Column(nullable = false)
    private Integer agenceEnvoiId;

    /** Agence de destination imposée à l'envoi (peut être null si libre) */
    @Column
    private Integer agenceReceptionId; 

    /** Utilisateur ayant créé l'envoi */
    @Column(nullable = false)
    private Integer utilisateurId; 

    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    // ===========================================
    // CHAMPS POUR LE RETRAIT (LOGIQUE PRO)
    // ===========================================

    @Column
    private LocalDateTime dateRetrait;

    /** ID de l'utilisateur qui a validé le paiement au bénéficiaire */
    @Column
    private Integer utilisateurRetraitId; 

    /** ID de l'agence où l'argent a été réellement retiré (Historique) */
    @Column(name = "agence_effective_retrait_id")
    private Integer agenceEffectiveRetraitId; 

    // ===========================================
    // CHAMPS POUR L'ANNULATION
    // ===========================================

    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

    /** ID de l'utilisateur ayant procédé à l'annulation/remboursement */
    @Column(name = "utilisateur_annulation_id")
    private Integer utilisateurAnnulationId;
}