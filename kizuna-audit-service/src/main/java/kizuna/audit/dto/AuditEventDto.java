package kizuna.audit.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record AuditEventDto(
        String action,
        String entity,
        String entityId,
        String userId,
        String username,
        LocalDateTime timestamp,
        Serializable data
) implements Serializable {
}
