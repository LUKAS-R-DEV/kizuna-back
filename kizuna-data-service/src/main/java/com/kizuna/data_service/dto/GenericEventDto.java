package com.kizuna.data_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record GenericEventDto(String eventType,String entityId,String entity,  LocalDateTime timestamp,
                              Map<String, Object> data) {
}
