package com.kizuna.data_service.integration.read.dto;

public record IntegrationProductionSummaryDto(
        long totalOrders,
        long inProgressOrders,
        long finishedOrders,
        double efficiencyPercent
) {
}
