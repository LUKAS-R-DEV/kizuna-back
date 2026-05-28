package kizuna_notification_service.monitoring.incident.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Builder
@Data
@Document(collection = "Incidents")
public class Incident {
    @Id
    private String id;
    private String serviceName;
    private Status status;
    private String message;
    private LocalDateTime timestamp;
    private boolean notificationSent;
}

