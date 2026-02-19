package com.transfertapi.controllers;

import com.transfertapi.dto.LoginResponseDTO;
import com.transfertapi.entities.Utilisateur;
import com.transfertapi.repositories.UtilisateurRepository;
import com.transfertapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data) {

        try {
            // 1️⃣ Authentification (Spring Security)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            data.get("email"),
                            data.get("motDePasse")
                    )
            );

            // 2️⃣ Chargement utilisateur sécurisé
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(data.get("email"));

            // 3️⃣ Extraction du rôle MÉTIER depuis l’authority
            String authority = userDetails.getAuthorities()
                    .iterator().next().getAuthority(); // ROLE_ADMIN

            String role = authority.replace("ROLE_", ""); // ADMIN

            // 4️⃣ Génération du JWT (rôle métier)
            String token = jwtUtil.generateToken(
                    userDetails.getUsername(),
                    role
            );

            // 5️⃣ Chargement entité métier
            Utilisateur u = utilisateurRepository
                    .findByEmail(data.get("email"))
                    .orElseThrow();

            // 6️⃣ Réponse vers le client (contrat inchangé)
            LoginResponseDTO dto = new LoginResponseDTO(
                    token,
                    u.getId(),
                    u.getNom(),
                    u.getEmail(),
                    u.getRole().name(), // SOURCE UNIQUE
                    u.getAgence() != null ? u.getAgence().getId() : null,
                    u.getAgence() != null ? u.getAgence().getNom() : null
            );

            return ResponseEntity.ok(dto);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Identifiants incorrects"));

        } catch (DisabledException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
