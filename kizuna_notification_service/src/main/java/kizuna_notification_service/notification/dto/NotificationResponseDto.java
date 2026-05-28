package kizuna_notification_service.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponseDto(String id, String title, String message, LocalDateTime timestamp, boolean isRead) {
}
