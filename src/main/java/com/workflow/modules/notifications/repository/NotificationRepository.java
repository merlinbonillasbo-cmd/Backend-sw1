// modules/notifications/repository/NotificationRepository.java
package com.workflow.modules.notifications.repository;

import com.workflow.modules.notifications.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByIdUsuario(String idUsuario);

    List<Notification> findByIdUsuarioAndLeido(String idUsuario, boolean leido);

    long countByIdUsuarioAndLeido(String idUsuario, boolean leido);
}
