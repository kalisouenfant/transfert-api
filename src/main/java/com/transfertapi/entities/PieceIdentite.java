package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pieces_identite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PieceIdentite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePiece typePiece;

    @Column(nullable = false, unique = true)
    private String numero;

    private LocalDate dateExpiration;
    private String fichierScan;

    public enum TypePiece {
        CNI,
        PASSEPORT,
        PERMIS_CONDUIRE,
        CARTE_CONSULAIRE,
        AUTRE
    }
}
