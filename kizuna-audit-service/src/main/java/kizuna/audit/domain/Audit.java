package kizuna.audit.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")

public class Audit {
    @Id
    private String id;
    private String action;
    private String entity;
    private String entityId;

    private String username;
    private String userId;

    private LocalDateTime timestamp;

    private Map<String, Object> details;
}
