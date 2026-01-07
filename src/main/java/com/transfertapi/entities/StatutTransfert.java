package com.transfertapi.entities;

public enum StatutTransfert {
    ENVOYE,   // Code généré et en attente de retrait
    RETIRE,   // Code utilisé pour retrait
    EXPIRE,   // Code expiré (délai dépassé)
    ANNULE    // Transfert annulé
}
