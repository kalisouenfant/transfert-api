package com.transfertapi.repositories;

import com.transfertapi.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByClientId(Integer clientId);

    List<Notification> findByStatut(Notification.Statut statut);

    List<Notification> findByType(Notification.Type type);
}
