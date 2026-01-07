package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "devises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Devise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String symbole;
}
