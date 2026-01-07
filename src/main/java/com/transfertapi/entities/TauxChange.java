package com.transfertapi.entities;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "taux_change")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TauxChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "devise_source_id", nullable = false)
    private Devise deviseSource;

    @ManyToOne
    @JoinColumn(name = "devise_cible_id", nullable = false)
    private Devise deviseCible;

    @Column(nullable = false)
    private BigDecimal taux;

    private LocalDate dateApplication = LocalDate.now();
}
