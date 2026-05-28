package com.kizuna.data_service.integration.read.dto;

import java.time.LocalDateTime;

public record IntegrationInventoryItemDto(
        Long inventoryId,
        String inventoryName,
        String status,
        String eventType,
        Number quantity,
        LocalDateTime timestamp
) {
}
