package com.transfertapi.controllers;

import com.transfertapi.dto.LoginResponseDTO;
import com.transfertapi.entities.Utilisateur;
import com.transfertapi.repositories.UtilisateurRepository;
import com.transfertapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // ================================
    //           LOGIN
    // ================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data) {
        try {
            // Authentification
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            data.get("email"),
                            data.get("motDePasse")
                    )
            );

            // Récupérer UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(data.get("email"));
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            // Générer JWT
            String token = jwtUtil.generateToken(userDetails.getUsername(), role);

            // Récupérer un utilisateur propre
            Utilisateur u = utilisateurRepository.findByEmail(data.get("email")).orElseThrow();

            // Construire une réponse DTO propre (sans entités JPA)
            LoginResponseDTO dto = new LoginResponseDTO(
                    token,
                    u.getId(),
                    u.getNom(),
                    u.getEmail(),
                    u.getRole().name(),
                    u.getAgence() != null ? u.getAgence().getId() : null,
                    u.getAgence() != null ? u.getAgence().getNom() : null
            );

            return ResponseEntity.ok(dto);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Identifiants incorrects"));
        }
    }

    // ================================
    //           REGISTER
    // ================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {

        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email déjà utilisé"));
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String token = jwtUtil.generateToken(userDetails.getUsername(), role);

        LoginResponseDTO dto = new LoginResponseDTO(
                token,
                saved.getId(),
                saved.getNom(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getAgence() != null ? saved.getAgence().getId() : null,
                saved.getAgence() != null ? saved.getAgence().getNom() : null
        );

        return ResponseEntity.ok(dto);
    }

    // ================================
    //          VERIFY TOKEN
    // ================================
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;

            String username = jwtUtil.extractUsername(jwt);
            boolean valid = jwtUtil.validateToken(jwt, username);

            return ResponseEntity.ok(Map.of(
                    "tokenValide", valid,
                    "utilisateur", username
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", "Token invalide ou expiré"));
        }
    }
}
