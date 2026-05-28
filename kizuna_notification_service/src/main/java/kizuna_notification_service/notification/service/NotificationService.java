package kizuna_notification_service.notification.service;

import kizuna_notification_service.config.dto.ApiResponseGeneric;
import kizuna_notification_service.config.exception.NotFoundException;
import kizuna_notification_service.notification.domain.Notification;
import kizuna_notification_service.notification.dto.NotificationResponseDto;
import kizuna_notification_service.notification.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;


    public List<NotificationResponseDto> findAll(String userId){
        return notificationRepository.findByUserId(userId).stream().map(this::notificationResponseDto).toList();
    }

    public  NotificationResponseDto findById(String notificationId, String userId){
        return notificationRepository.findByIdAndUserId(notificationId, userId).map(this::notificationResponseDto).orElseThrow(() -> new NotFoundException ("Notification not found"));
    }

    public ApiResponseGeneric delete(String notificationId, String userId){
        Notification notification = notificationRepository.findByIdAndUserId(notificationId,userId).orElseThrow(()-> new NotFoundException("Notification not found"));
        notificationRepository.delete(notification);
        return apiResponseGeneric("Notification deleted successfully");
    }
    public ApiResponseGeneric markIsRead(String notificationId, String userId){
        Notification notification = notificationRepository.findByIdAndUserId(notificationId,userId).orElseThrow(()-> new NotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
        return apiResponseGeneric("Notification marked as read successfully");
    }

private NotificationResponseDto notificationResponseDto(Notification notification){
    return new NotificationResponseDto(notification.getId(), notification.getTitle(),notification.getMessage(),notification.getTimestamp(),notification.isRead());
}
private ApiResponseGeneric apiResponseGeneric(String message){
    return new ApiResponseGeneric(message, LocalDateTime.now());
}

}
