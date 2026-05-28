package com.kizuna.data_service.integration.read.dto;

import java.time.LocalDateTime;

public record IntegrationMetricsSummaryDto(
        IntegrationProductionSummaryDto production,
        IntegrationInventorySummaryDto inventory,
        IntegrationQualitySummaryDto quality,
        String periodLabel,
        LocalDateTime from,
        LocalDateTime to
) {
}
