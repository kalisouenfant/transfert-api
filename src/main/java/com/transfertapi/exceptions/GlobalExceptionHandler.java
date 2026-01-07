package com.transfertapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // --------------------------------------------------------
    // 🔹 1. Erreurs de validation (champs invalides du DTO)
    // Code HTTP: 400 Bad Request
    // --------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // --------------------------------------------------------
    // 🔹 2. Erreurs de logique métier (Solde insuffisant, Statut invalide)
    // Utilisé pour les RuntimeException levées dans CodeTransfertService
    // Code HTTP: 400 Bad Request
    // --------------------------------------------------------
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessLogicException(RuntimeException ex, WebRequest request) {
        // NOTE: Cette méthode est très générale. Elle doit être placée APRÈS les exceptions
        // plus spécifiques (comme ResourceNotFoundException) pour ne pas les masquer.
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", new Date());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Business Logic Error / Conflict");
        errorDetails.put("message", ex.getMessage()); 
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    
    // --------------------------------------------------------
    // 🔹 3. Erreurs d'arguments/validation fonctionnelle (Montant invalide dans TransactionService)
    // Utilisé pour les IllegalArgumentException.
    // Code HTTP: 400 Bad Request
    // --------------------------------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", new Date());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Invalid Argument");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // --------------------------------------------------------
    // 🔹 4. Ressource non trouvée
    // Code HTTP: 404 Not Found
    // --------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        // L'API renvoie le message d'erreur avec un statut 404.
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // --------------------------------------------------------
    // 🔹 5. Gestion de toutes les autres erreurs (dernier recours)
    // Code HTTP: 500 Internal Server Error
    // --------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        // ex.printStackTrace(); // Utile pour le debug en développement
        return new ResponseEntity<>("Erreur interne du serveur : " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}