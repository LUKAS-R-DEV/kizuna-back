package kizuna_notification_service.notification.controller;

import kizuna_notification_service.config.dto.ApiResponseGeneric;
import kizuna_notification_service.notification.dto.NotificationResponseDto;
import kizuna_notification_service.notification.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationService notificationService;


    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> findAll(@AuthenticationPrincipal Jwt jwt){
        String userId = jwt.getSubject();
        return ResponseEntity.ok(notificationService.findAll(userId));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponseDto> findById(@PathVariable String notificationId,@AuthenticationPrincipal Jwt jwt){
        String userId=jwt.getSubject();
        return ResponseEntity.ok(notificationService.findById(notificationId, userId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseGeneric> delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.delete(id, userId));
    }
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponseGeneric> markIsRead(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.markIsRead(id, userId));
    }

}
