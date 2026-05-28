package com.kizuna.data_service.metrics.dtos;

import java.time.LocalDateTime;

public record OeeMetricsDto(
        double availabilityPercent,
        double qualityPercent,
        double oeePercent,
        long completedProductionOrders,
        long totalProductionOrders,
        long approvedInspections,
        long totalInspections,
        String periodLabel,
        LocalDateTime from,
        LocalDateTime to
) {
}
