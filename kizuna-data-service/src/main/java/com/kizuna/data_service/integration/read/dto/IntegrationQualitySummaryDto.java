package com.kizuna.data_service.integration.read.dto;

public record IntegrationQualitySummaryDto(
        long approved,
        long rejected,
        long rework,
        double rejectionRatePercent,
        double yieldPercent
) {
}
