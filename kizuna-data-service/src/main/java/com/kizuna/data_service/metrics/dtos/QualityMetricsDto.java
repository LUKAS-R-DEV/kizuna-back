package com.kizuna.data_service.metrics.dtos;

import java.time.LocalDateTime;

public record QualityMetricsDto(
        Long aprovedOrders,
        Long rejectedOrders,
        Long reworkOrders,
        Double rejectionRatePercent,
        Double yieldPercent,
        String periodLabel,
        LocalDateTime from,
        LocalDateTime to
) {
    public QualityMetricsDto(Long aprovedOrders, Long rejectedOrders) {
        this(aprovedOrders, rejectedOrders, 0L, null, null, null, null, null);
    }
}
