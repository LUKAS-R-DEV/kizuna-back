package com.kizuna.data_service.integration.read.dto;

public record IntegrationInventorySummaryDto(
        long totalMovements,
        long lowStockMovements,
        long normalStockMovements
) {
}
