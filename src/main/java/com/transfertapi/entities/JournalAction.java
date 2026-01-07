package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité d'audit pour tracer les actions des utilisateurs.
 * Le constructeur à 6 arguments est utilisé par CodeTransfertService.
 */
@Entity
@Table(name = "journal_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Nécessaire pour le constructeur à 6 arguments (id, utilisateurId, action, description, dateAction, ip)
@Builder // Facultatif, mais utile pour d'autres usages
public class JournalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "utilisateur_id", nullable = false)
    private Integer utilisateurId;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_action")
    private LocalDateTime dateAction = LocalDateTime.now(); // Valeur par défaut JPA/Hibernate, mais le service la fournit explicitement.

    @Column(length = 50)
    private String ip; // Utilisé comme 6ème argument 'null' dans CodeTransfertService

    // NOTE: Si vous vouliez ajouter l'ID de l'entité concernée (ex: l'ID du CodeTransfert),
    // vous devriez ajouter un champ 'entiteId' ici et ajuster le constructeur dans CodeTransfertService.
    // Pour l'instant, nous nous en tenons à la structure de vos fichiers.
}