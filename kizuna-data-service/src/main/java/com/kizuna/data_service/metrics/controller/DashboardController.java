package com.kizuna.data_service.metrics.controller;

import com.kizuna.data_service.metrics.dtos.InventoryMetricsDto;
import com.kizuna.data_service.metrics.dtos.OeeMetricsDto;
import com.kizuna.data_service.metrics.dtos.ProductionMetricsDto;
import com.kizuna.data_service.metrics.dtos.QualityMetricsDto;
import com.kizuna.data_service.metrics.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/inventory")
    public ResponseEntity<InventoryMetricsDto> inventoryMetrics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(dashboardService.getInventoryMetrics(period, from, to));
    }

    @GetMapping("/production")
    public ResponseEntity<ProductionMetricsDto> productionMetrics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(dashboardService.getProductionMetrics(period, from, to));
    }

    @GetMapping("/quality")
    public ResponseEntity<QualityMetricsDto> qualityMetrics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(dashboardService.getQualityMetrics(period, from, to));
    }

    @GetMapping("/oee")
    public ResponseEntity<OeeMetricsDto> oeeMetrics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(dashboardService.getOeeMetrics(period, from, to));
    }
}
