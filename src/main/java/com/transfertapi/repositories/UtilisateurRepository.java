package com.transfertapi.repositories;

import com.transfertapi.entities.Utilisateur;
import com.transfertapi.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    // Authentification
    Optional<Utilisateur> findByEmail(String email);

    // Vérification de doublons
    boolean existsByEmail(String email);

    // Filtres de listes
    List<Utilisateur> findByRole(Role role);
    List<Utilisateur> findByAgenceId(Integer agenceId);

    // FIX : Pour AgenceService (Vérifie si l'agence possède des employés avant suppression)
    boolean existsByAgenceId(Integer agenceId);

    // Recherche pour les panels d'administration
    @Query("SELECT u FROM Utilisateur u WHERE " +
           "LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Utilisateur> searchUtilisateurs(@Param("search") String search);
}