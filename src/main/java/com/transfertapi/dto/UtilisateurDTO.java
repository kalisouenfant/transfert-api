package com.transfertapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UtilisateurDTO {
    private Integer id;
    private String nom;
    private String email;
    private String motDePasse;
    private String role;
    private Boolean actif;
    private Integer agenceId;
}
