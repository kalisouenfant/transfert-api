package com.transfertapi.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private Integer id;
    private String nom;
    private String email;
    private String role;
    private Integer agenceId;
    private String agenceNom;
}
