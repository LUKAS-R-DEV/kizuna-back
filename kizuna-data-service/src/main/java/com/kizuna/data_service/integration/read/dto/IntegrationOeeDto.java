package com.kizuna.data_service.integration.read.dto;

import java.time.LocalDateTime;

public record IntegrationOeeDto(
        double availabilityPercent,
        double qualityPercent,
        double oeePercent,
        long completedOrders,
        long totalOrders,
        String periodLabel,
        LocalDateTime from,
        LocalDateTime to
) {
}
