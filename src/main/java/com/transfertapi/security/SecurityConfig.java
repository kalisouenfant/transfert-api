package com.transfertapi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Ajouté pour filtrer par méthode HTTP
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .cors().configurationSource(corsConfigurationSource()).and()
            .authorizeRequests()
                // 1. Public (Auth et Console H2)
                .antMatchers("/api/auth/**", "/h2-console/**").permitAll()

                // 2. Statistiques : Accessible à tous les rôles authentifiés
                .antMatchers("/api/transactions/stats", "/api/transactions/stats/**")
                    .hasAnyRole("SUPERADMIN", "ADMIN", "RESPONSABLE", "AGENT")

                // 3. Agences : Correction recommandée
                // Permettre à ADMIN et RESPONSABLE de voir la liste (GET)
                .antMatchers(HttpMethod.GET, "/api/agences/**")
                    .hasAnyRole("SUPERADMIN", "ADMIN", "RESPONSABLE")
                // Seul le SUPERADMIN peut créer, modifier ou supprimer
                .antMatchers("/api/agences/**").hasRole("SUPERADMIN")

                // 4. Utilisateurs
                .antMatchers("/api/utilisateurs/minimal").authenticated()
                .antMatchers("/api/utilisateurs/**").hasAnyRole("SUPERADMIN", "ADMIN")

                // 5. Tout le reste (Transactions, Clients, Journal, Codes)
                .anyRequest().authenticated()

            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Frames pour H2
        http.headers().frameOptions().disable();

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Utilisation de pattern pour plus de flexibilité avec le client desktop
        config.setAllowedOriginPatterns(List.of("*")); 
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}