package kizuna.audit.dto;

import java.util.Map;

/** API view: timestamp as ISO-8601 UTC (aligned with server clock in the UI). */
public record AuditLogResponse(
        String id,
        String action,
        String entity,
        String entityId,
        String username,
        String userId,
        String timestamp,
        Map<String, Object> details
) {
}
