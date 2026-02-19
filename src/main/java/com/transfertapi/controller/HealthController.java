package com.transfertapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public HealthStatus health() {
        return new HealthStatus("Transfert API is up 🚀", Instant.now().toString());
    }

    // Classe interne pour un JSON propre
    public static class HealthStatus {
        private String status;
        private String timestamp;

        public HealthStatus(String status, String timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getStatus() { return status; }
        public String getTimestamp() { return timestamp; }

        public void setStatus(String status) { this.status = status; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
