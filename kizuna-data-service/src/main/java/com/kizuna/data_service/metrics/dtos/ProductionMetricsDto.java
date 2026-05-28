package com.kizuna.data_service.metrics.dtos;

import java.time.LocalDateTime;

public record ProductionMetricsDto(
        Long totalOrders,
        Long StartedOrders,
        Long finishedOrders,
        Long plannedOrders,
        Long pausedOrders,
        Long waitingInspectionOrders,
        Long cancelledOrders,
        Double efficiencyPercent,
        String periodLabel,
        LocalDateTime from,
        LocalDateTime to
) {
    public ProductionMetricsDto(Long totalOrders, Long StartedOrders, Long finishedOrders) {
        this(totalOrders, StartedOrders, finishedOrders, 0L, 0L, 0L, 0L, null, null, null, null);
    }
}
