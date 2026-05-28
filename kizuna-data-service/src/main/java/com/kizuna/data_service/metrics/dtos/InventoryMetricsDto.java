package com.kizuna.data_service.metrics.dtos;

import java.time.LocalDateTime;

public record InventoryMetricsDto(
        Long totalItems,
        Long lowStockItems,
        Long healthyItems,
        String periodLabel,
        LocalDateTime from,
        LocalDateTime to
) {
    public InventoryMetricsDto(Long totalItems, Long lowStockItems) {
        this(totalItems, lowStockItems, Math.max(0, totalItems - lowStockItems), null, null, null);
    }
}
