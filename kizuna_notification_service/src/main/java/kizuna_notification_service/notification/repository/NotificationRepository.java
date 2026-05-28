package kizuna_notification_service.notification.repository;

import kizuna_notification_service.notification.domain.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
    Optional <Notification> findByIdAndUserId(String notificationId, String userId);
}
