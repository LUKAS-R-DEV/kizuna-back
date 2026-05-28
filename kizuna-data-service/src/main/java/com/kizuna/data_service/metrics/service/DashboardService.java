package com.kizuna.data_service.metrics.service;

import com.kizuna.data_service.domain.ProductionEvent;
import com.kizuna.data_service.domain.QualityInspectionEvent;
import com.kizuna.data_service.domain.InventoryEvent;
import com.kizuna.data_service.metrics.MetricsPeriodResolver;
import com.kizuna.data_service.metrics.MetricsPeriodResolver.Period;
import com.kizuna.data_service.metrics.dtos.InventoryMetricsDto;
import com.kizuna.data_service.metrics.dtos.OeeMetricsDto;
import com.kizuna.data_service.metrics.dtos.ProductionMetricsDto;
import com.kizuna.data_service.metrics.dtos.QualityMetricsDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@AllArgsConstructor
@Service
public class DashboardService {

    private static final Set<String> IN_PROGRESS_STATUSES = Set.of(
            "START", "IN_PROGRESS", "RESUME", "PAUSED", "REWORK"
    );
    private static final Set<String> FINISHED_STATUSES = Set.of(
            "FINISH", "WAITING_INSPECTION", "APPROVED", "FINISHED_BY_TIME"
    );

    private final EventQueryService eventQueryService;

    public ProductionMetricsDto getProductionMetrics() {
        return getProductionMetrics(null, null, "30d");
    }

    public ProductionMetricsDto getProductionMetrics(String period, String from, String to) {
        Period range = MetricsPeriodResolver.resolve(period, from, to);
        List<ProductionEvent> events = eventQueryService.productionEvents(period, from, to, "ALL");

        long total = events.size();
        long inProgress = countByStatus(events, IN_PROGRESS_STATUSES);
        long finished = countByStatus(events, FINISHED_STATUSES);
        long planned = countByStatus(events, Set.of("PLANNED"));
        long paused = countByStatus(events, Set.of("PAUSED"));
        long waitingInspection = countByStatus(events, Set.of("WAITING_INSPECTION"));
        long cancelled = countByStatus(events, Set.of("CANCELLED"));

        double efficiency = total == 0 ? 0.0 : round2((finished * 100.0) / total);

        return new ProductionMetricsDto(
                total,
                inProgress,
                finished,
                planned,
                paused,
                waitingInspection,
                cancelled,
                efficiency,
                range.label(),
                range.from(),
                range.to()
        );
    }

    public InventoryMetricsDto getInventoryMetrics() {
        return getInventoryMetrics(null, null, "30d");
    }

    public InventoryMetricsDto getInventoryMetrics(String period, String from, String to) {
        Period range = MetricsPeriodResolver.resolve(period, from, to);
        List<InventoryEvent> events = eventQueryService.inventoryEvents(period, from, to, false);

        long total = events.size();
        long lowStock = events.stream()
                .filter(e -> e.getQuantity() != null && e.getQuantity().intValue() < 10)
                .count();

        return new InventoryMetricsDto(
                total,
                lowStock,
                Math.max(0, total - lowStock),
                range.label(),
                range.from(),
                range.to()
        );
    }

    public QualityMetricsDto getQualityMetrics() {
        return getQualityMetrics(null, null, "30d");
    }

    public QualityMetricsDto getQualityMetrics(String period, String from, String to) {
        Period range = MetricsPeriodResolver.resolve(period, from, to);
        List<QualityInspectionEvent> events = eventQueryService.qualityEvents(period, from, to, "ALL");

        long approved = countByResult(events, "APPROVED");
        long rejected = countByResult(events, "REJECTED");
        long rework = countByResult(events, "REWORK");

        long inspected = approved + rejected + rework;
        double rejectionRate = inspected == 0 ? 0.0 : round2((rejected * 100.0) / inspected);
        double yield = inspected == 0 ? 0.0 : round2((approved * 100.0) / inspected);

        return new QualityMetricsDto(
                approved,
                rejected,
                rework,
                rejectionRate,
                yield,
                range.label(),
                range.from(),
                range.to()
        );
    }

    public OeeMetricsDto getOeeMetrics(String period, String from, String to) {
        ProductionMetricsDto production = getProductionMetrics(period, from, to);
        QualityMetricsDto quality = getQualityMetrics(period, from, to);

        long totalProd = production.totalOrders() == null ? 0 : production.totalOrders();
        long completed = production.finishedOrders() == null ? 0 : production.finishedOrders();

        long approved = quality.aprovedOrders() == null ? 0 : quality.aprovedOrders();
        long rejected = quality.rejectedOrders() == null ? 0 : quality.rejectedOrders();
        long rework = quality.reworkOrders() == null ? 0 : quality.reworkOrders();
        long totalInspections = approved + rejected + rework;

        double availability = totalProd == 0 ? 0.0 : round2((completed * 100.0) / totalProd);
        double qualityRate = totalInspections == 0 ? 100.0 : round2((approved * 100.0) / totalInspections);
        double oee = round2((availability * qualityRate) / 100.0);

        return new OeeMetricsDto(
                availability,
                qualityRate,
                oee,
                completed,
                totalProd,
                approved,
                totalInspections,
                production.periodLabel(),
                production.from(),
                production.to()
        );
    }

    private static long countByStatus(List<ProductionEvent> events, Set<String> statuses) {
        return events.stream()
                .filter(e -> statuses.contains(normalizeStatus(e)))
                .count();
    }

    private static String normalizeStatus(ProductionEvent event) {
        if (event.getStatus() != null && !event.getStatus().isBlank()) {
            return event.getStatus().trim().toUpperCase(Locale.ROOT);
        }
        String type = event.getType();
        if (type == null) {
            return "";
        }
        String upper = type.toUpperCase(Locale.ROOT);
        if (upper.contains("FINISHED")) {
            return "FINISH";
        }
        if (upper.contains("STARTED")) {
            return "START";
        }
        if (upper.contains("PAUSED")) {
            return "PAUSED";
        }
        if (upper.contains("REWORK")) {
            return "REWORK";
        }
        return upper;
    }

    private static long countByResult(List<QualityInspectionEvent> events, String result) {
        return events.stream()
                .filter(e -> result.equalsIgnoreCase(safe(e.getResult())))
                .count();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
