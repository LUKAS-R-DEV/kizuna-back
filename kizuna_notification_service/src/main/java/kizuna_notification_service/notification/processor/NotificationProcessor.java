package kizuna_notification_service.notification.processor;

import kizuna_notification_service.notification.domain.Notification;
import kizuna_notification_service.notification.dto.GenericEventDto;
import kizuna_notification_service.notification.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class NotificationProcessor {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;



    public void process (GenericEventDto eventDto){
        if (eventDto.userId() == null || eventDto.userId().isBlank()) {
            System.err.println("[NOTIFICATION] Invalid message: userId is null or blank");
            return;
        }
        handleNotification(eventDto);
    }

    public void handleNotification(GenericEventDto eventDto) {
        Notification notification = Notification.builder()
                .title((String) eventDto.data().get("title"))
                .userId((String) eventDto.data().get("userId"))
                .message((String) eventDto.data().get("message"))
                .timestamp(LocalDateTime.now())
                .type((String) eventDto.data().get("eventType"))
                .targetRole((String) eventDto.data().get("targetRole"))
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        if(notification.getTargetRole() != null && !notification.getTargetRole().isBlank()){
            String roleTopic = "/topic/role/" + notification.getTargetRole();
            messagingTemplate.convertAndSend(roleTopic, notification);
        }
        if(notification.getUserId() != null && !notification.getUserId().isBlank()){
            String userTopic = "/topic/user/" + notification.getUserId();
            messagingTemplate.convertAndSend(userTopic, notification);
        }
        System.out.println("[NOTIFICATION] Processada e enviada via WebSocket");

    }


}
