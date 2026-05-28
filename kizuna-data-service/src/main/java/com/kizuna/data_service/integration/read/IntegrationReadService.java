package com.kizuna.data_service.integration.read;

import com.kizuna.data_service.domain.InventoryEvent;
import com.kizuna.data_service.domain.ProductionEvent;
import com.kizuna.data_service.domain.QualityInspectionEvent;
import com.kizuna.data_service.integration.config.IntegrationProperties;
import com.kizuna.data_service.integration.read.dto.*;
import com.kizuna.data_service.metrics.dtos.InventoryMetricsDto;
import com.kizuna.data_service.metrics.dtos.OeeMetricsDto;
import com.kizuna.data_service.metrics.dtos.ProductionMetricsDto;
import com.kizuna.data_service.metrics.dtos.QualityMetricsDto;
import com.kizuna.data_service.metrics.service.DashboardService;
import com.kizuna.data_service.metrics.service.EventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationReadService {

    private final DashboardService dashboardService;
    private final EventQueryService eventQueryService;
    private final IntegrationProperties properties;

    public IntegrationInfoDto info() {
        return new IntegrationInfoDto(
                "v1",
                "kizuna-data-service",
                true,
                Instant.now()
        );
    }

    public IntegrationMetricsSummaryDto metricsSummary(String period, String from, String to) {
        ProductionMetricsDto production = dashboardService.getProductionMetrics(period, from, to);
        InventoryMetricsDto inventory = dashboardService.getInventoryMetrics(period, from, to);
        QualityMetricsDto quality = dashboardService.getQualityMetrics(period, from, to);

        return new IntegrationMetricsSummaryDto(
                new IntegrationProductionSummaryDto(
                        safeLong(production.totalOrders()),
                        safeLong(production.StartedOrders()),
                        safeLong(production.finishedOrders()),
                        production.efficiencyPercent() == null ? 0.0 : production.efficiencyPercent()
                ),
                new IntegrationInventorySummaryDto(
                        safeLong(inventory.totalItems()),
                        safeLong(inventory.lowStockItems()),
                        safeLong(inventory.healthyItems())
                ),
                new IntegrationQualitySummaryDto(
                        safeLong(quality.aprovedOrders()),
                        safeLong(quality.rejectedOrders()),
                        safeLong(quality.reworkOrders()),
                        quality.rejectionRatePercent() == null ? 0.0 : quality.rejectionRatePercent(),
                        quality.yieldPercent() == null ? 0.0 : quality.yieldPercent()
                ),
                production.periodLabel(),
                production.from(),
                production.to()
        );
    }

    public IntegrationOeeDto oee(String period, String from, String to) {
        OeeMetricsDto oee = dashboardService.getOeeMetrics(period, from, to);
        return new IntegrationOeeDto(
                oee.availabilityPercent(),
                oee.qualityPercent(),
                oee.oeePercent(),
                oee.completedProductionOrders(),
                oee.totalProductionOrders(),
                oee.periodLabel(),
                oee.from(),
                oee.to()
        );
    }

    public List<IntegrationProductionOrderDto> productionOrders(
            String period,
            String from,
            String to,
            String status,
            int limit
    ) {
        return eventQueryService.productionEvents(period, from, to, status).stream()
                .limit(clampLimit(limit))
                .map(this::toProductionOrder)
                .toList();
    }

    public List<IntegrationInventoryItemDto> inventoryItems(
            String period,
            String from,
            String to,
            Boolean lowStockOnly,
            int limit
    ) {
        return eventQueryService.inventoryEvents(period, from, to, lowStockOnly).stream()
                .limit(clampLimit(limit))
                .map(this::toInventoryItem)
                .toList();
    }

    public List<IntegrationQualityInspectionDto> qualityInspections(
            String period,
            String from,
            String to,
            String result,
            int limit
    ) {
        return eventQueryService.qualityEvents(period, from, to, result).stream()
                .limit(clampLimit(limit))
                .map(this::toQualityInspection)
                .toList();
    }

    private IntegrationProductionOrderDto toProductionOrder(ProductionEvent event) {
        return new IntegrationProductionOrderDto(
                event.getOrderId(),
                event.getRecipeName(),
                event.getStatus(),
                event.getType(),
                event.getTimestamp()
        );
    }

    private IntegrationInventoryItemDto toInventoryItem(InventoryEvent event) {
        return new IntegrationInventoryItemDto(
                event.getInventoryId(),
                event.getInventoryName(),
                event.getStatus(),
                event.getType(),
                event.getQuantity(),
                event.getTimestamp()
        );
    }

    private IntegrationQualityInspectionDto toQualityInspection(QualityInspectionEvent event) {
        return new IntegrationQualityInspectionDto(
                event.getOrderId(),
                event.getResult(),
                event.getType(),
                event.getTimestamp()
        );
    }

    private int clampLimit(int limit) {
        if (limit <= 0) {
            return 50;
        }
        return Math.min(limit, properties.maxPageSize());
    }

    private static long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
