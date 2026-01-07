package com.transfertapi.services;

import com.transfertapi.entities.Notification;
import com.transfertapi.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // 🔹 Enregistrer une notification
    public Notification save(Notification notification) {
        notification.setDateCreation(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    // 🔹 Récupérer toutes les notifications
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    // 🔹 Récupérer une notification spécifique
    public Optional<Notification> getById(Integer id) {
        return notificationRepository.findById(id);
    }

    // 🔹 Récupérer par client
    public List<Notification> getByClient(Integer clientId) {
        return notificationRepository.findByClientId(clientId);
    }

    // 🔹 Récupérer par statut
    public List<Notification> getByStatut(Notification.Statut statut) {
        return notificationRepository.findByStatut(statut);
    }

    // 🔹 Supprimer une notification
    public void delete(Integer id) {
        notificationRepository.deleteById(id);
    }

    // 🔹 Marquer une notification comme envoyée
    public Notification marquerCommeEnvoyee(Integer id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) {
            Notification notif = opt.get();
            notif.setStatut(Notification.Statut.ENVOYE);
            notif.setDateEnvoi(LocalDateTime.now());
            return notificationRepository.save(notif);
        }
        return null;
    }
}
