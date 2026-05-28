package com.kizuna.data_service.integration.read;

import com.kizuna.data_service.integration.read.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/integration/v1")
public class IntegrationReadController {

    private final IntegrationReadService integrationReadService;

    @GetMapping("/info")
    public IntegrationInfoDto info() {
        return integrationReadService.info();
    }

    @GetMapping("/metrics/summary")
    public IntegrationMetricsSummaryDto metricsSummary(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return integrationReadService.metricsSummary(period, from, to);
    }

    @GetMapping("/metrics/oee")
    public IntegrationOeeDto oee(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return integrationReadService.oee(period, from, to);
    }

    @GetMapping("/production/orders")
    public ListResponse<IntegrationProductionOrderDto> productionOrders(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        var items = integrationReadService.productionOrders(period, from, to, status, limit);
        return new ListResponse<>(items.size(), items);
    }

    @GetMapping("/inventory/items")
    public ListResponse<IntegrationInventoryItemDto> inventoryItems(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(defaultValue = "50") int limit
    ) {
        var items = integrationReadService.inventoryItems(period, from, to, lowStockOnly, limit);
        return new ListResponse<>(items.size(), items);
    }

    @GetMapping("/quality/inspections")
    public ListResponse<IntegrationQualityInspectionDto> qualityInspections(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "ALL") String result,
            @RequestParam(defaultValue = "50") int limit
    ) {
        var items = integrationReadService.qualityInspections(period, from, to, result, limit);
        return new ListResponse<>(items.size(), items);
    }

    public record ListResponse<T>(int count, java.util.List<T> items) {
    }
}
