package com.transfertapi.services;

import com.transfertapi.entities.JournalAction;
import com.transfertapi.repositories.JournalActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuditService {

    @Autowired
    private JournalActionRepository journalRepo;

    // ✅ Méthode principale avec HttpServletRequest
    public void enregistrerAction(Integer utilisateurId, String action, String description, HttpServletRequest request) {
        String ip = request != null ? request.getRemoteAddr() : "N/A";

        JournalAction log = new JournalAction();
        log.setUtilisateurId(utilisateurId);
        log.setAction(action);
        log.setDescription(description);
        log.setDateAction(LocalDateTime.now());
        log.setIp(ip);
        journalRepo.save(log);
    }

    // ✅ Surcharge : version sans HttpServletRequest
    public void enregistrerAction(Integer utilisateurId, String action, String description) {
        enregistrerAction(utilisateurId, action, description, null);
    }
}
