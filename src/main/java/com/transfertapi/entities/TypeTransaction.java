package com.transfertapi.entities;

public enum TypeTransaction {
    DEPOT,
    RETRAIT,
    // ✅ Nouveaux types pour les transactions électroniques (Mobile Money, Wave, etc.)
    M_DEPOT, 
    M_RETRAIT,
    
    TRANSFERT_ENVOI,
    TRANSFERT_RECEPTION,
    TRANSFERT_ANNULATION
}