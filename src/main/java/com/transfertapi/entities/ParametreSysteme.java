package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "parametres_systeme")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ParametreSysteme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String cle;

    @Column(nullable = false)
    private String valeur;

    private String description;
}
