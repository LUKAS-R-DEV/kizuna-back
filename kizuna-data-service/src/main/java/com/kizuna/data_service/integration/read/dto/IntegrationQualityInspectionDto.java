package com.kizuna.data_service.integration.read.dto;

import java.time.LocalDateTime;

public record IntegrationQualityInspectionDto(
        Long orderId,
        String result,
        String eventType,
        LocalDateTime timestamp
) {
}
