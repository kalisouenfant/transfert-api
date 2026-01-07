package com.transfertapi.entities;

/**
 * Définit les motifs standardisés pour les mouvements de caisse (transactions génériques
 * qui ne sont pas des transferts P2P). Cela remplace la saisie libre.
 */
public enum TypeMotifCaisse {
    
    // Motifs liés aux opérateurs mobiles
    DEPOT_OM("Dépôt Orange Money"),
    RETRAIT_OM("Retrait Orange Money"),
    DEPOT_MM("Dépôt Mobile Money"),
    RETRAIT_MM("Retrait Mobile Money"),

    // Motifs liés aux opérations internes/administratives
    OPERATION_ADMIN("Opération Administrative"),
    DEPENSE_COURANTE("Dépense Courante (Loyer, Fournitures)"),
    MISE_FONDS("Mise à disposition de fonds (Entrée)"),
    RECUP_FONDS("Récupération de fonds (Sortie)"),

    // Autres dépôts/retraits simples
    DEPOT_SIMPLE_CLIENT("Dépôt Simple Client"),
    RETRAIT_SIMPLE_CLIENT("Retrait Simple Client");


    private final String libel;

    TypeMotifCaisse(String libel) {
        this.libel = libel;
    }

    public String getLibel() {
        return libel;
    }
}