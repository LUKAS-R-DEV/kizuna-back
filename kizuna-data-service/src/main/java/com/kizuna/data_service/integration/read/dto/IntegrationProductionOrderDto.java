package com.kizuna.data_service.integration.read.dto;

import java.time.LocalDateTime;

public record IntegrationProductionOrderDto(
        Long orderId,
        String recipeName,
        String status,
        String eventType,
        LocalDateTime timestamp
) {
}
