package com.kizuna.data_service.reports.controller;

import com.kizuna.data_service.domain.InventoryEvent;
import com.kizuna.data_service.domain.ProductionEvent;
import com.kizuna.data_service.domain.QualityInspectionEvent;
import com.kizuna.data_service.metrics.service.EventQueryService;
import com.kizuna.data_service.reports.service.ReportsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/report")
public class ReportController {
    private final ReportsService reportsService;
    private final EventQueryService eventQueryService;

    @GetMapping({"/pdf", "/export"})
    public ResponseEntity<byte[]> generate(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        byte[] pdf = reportsService.generatePDFReport(period, from, to);

        String fileName = "KIZUNA_ANALYTICS_REPORT_" + LocalDateTime.now().getNano() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/production-events")
    public ResponseEntity<List<ProductionEvent>> getProductionEvents(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(eventQueryService.productionEvents(period, from, to, status));
    }

    @GetMapping("/inventory-events")
    public ResponseEntity<List<InventoryEvent>> getInventoryEvents(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Boolean lowStockOnly) {
        return ResponseEntity.ok(eventQueryService.inventoryEvents(period, from, to, lowStockOnly));
    }

    @GetMapping("/quality-events")
    public ResponseEntity<List<QualityInspectionEvent>> getQualityEvents(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String result) {
        return ResponseEntity.ok(eventQueryService.qualityEvents(period, from, to, result));
    }
}
