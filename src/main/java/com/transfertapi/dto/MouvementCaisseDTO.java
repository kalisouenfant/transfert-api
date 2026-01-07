package com.transfertapi.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MouvementCaisseDTO {

    private Integer id;

    private Integer agenceId;
    private String agenceNom;

    private String type;
    private BigDecimal montant;
    private String motif;

    private Integer utilisateurId;
    private String utilisateurNom;

    private LocalDateTime dateMouvement;
}
