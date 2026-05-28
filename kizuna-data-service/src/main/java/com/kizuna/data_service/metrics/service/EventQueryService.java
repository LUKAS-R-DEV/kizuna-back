package com.kizuna.data_service.metrics.service;

import com.kizuna.data_service.domain.InventoryEvent;
import com.kizuna.data_service.domain.ProductionEvent;
import com.kizuna.data_service.domain.QualityInspectionEvent;
import com.kizuna.data_service.metrics.MetricsPeriodResolver;
import com.kizuna.data_service.repository.InventoryEventRepository;
import com.kizuna.data_service.repository.ProductionEventRepository;
import com.kizuna.data_service.repository.QualityInspectionEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventQueryService {

    private final ProductionEventRepository productionRepository;
    private final InventoryEventRepository inventoryRepository;
    private final QualityInspectionEventRepository qualityRepository;

    public List<ProductionEvent> productionEvents(
            String period, String from, String to, String status) {
        MetricsPeriodResolver.Period range = MetricsPeriodResolver.resolve(period, from, to);
        List<ProductionEvent> events = loadProductionInRange(range);
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            String normalized = status.trim().toUpperCase();
            events = events.stream()
                    .filter(e -> normalized.equalsIgnoreCase(safe(e.getStatus()))
                            || normalized.equalsIgnoreCase(safe(e.getType())))
                    .toList();
        }
        return sortByTimestampDesc(events);
    }

    public List<QualityInspectionEvent> qualityEvents(
            String period, String from, String to, String result) {
        MetricsPeriodResolver.Period range = MetricsPeriodResolver.resolve(period, from, to);
        List<QualityInspectionEvent> events = loadQualityInRange(range);
        if (result != null && !result.isBlank() && !"ALL".equalsIgnoreCase(result)) {
            String normalized = result.trim().toUpperCase();
            events = events.stream()
                    .filter(e -> normalized.equalsIgnoreCase(safe(e.getResult())))
                    .toList();
        }
        return sortByTimestampDesc(events);
    }

    public List<InventoryEvent> inventoryEvents(
            String period, String from, String to, Boolean lowStockOnly) {
        MetricsPeriodResolver.Period range = MetricsPeriodResolver.resolve(period, from, to);
        List<InventoryEvent> events = loadInventoryInRange(range);
        if (Boolean.TRUE.equals(lowStockOnly)) {
            events = events.stream()
                    .filter(e -> e.getQuantity() != null && e.getQuantity().intValue() < 10)
                    .toList();
        }
        return sortByTimestampDesc(events);
    }

    private List<ProductionEvent> loadProductionInRange(MetricsPeriodResolver.Period range) {
        if ("all".equals(range.label())) {
            return productionRepository.findAll();
        }
        return productionRepository.findByTimestampBetween(range.from(), range.to());
    }

    private List<QualityInspectionEvent> loadQualityInRange(MetricsPeriodResolver.Period range) {
        if ("all".equals(range.label())) {
            return qualityRepository.findAll();
        }
        return qualityRepository.findByTimestampBetween(range.from(), range.to());
    }

    private List<InventoryEvent> loadInventoryInRange(MetricsPeriodResolver.Period range) {
        if ("all".equals(range.label())) {
            return inventoryRepository.findAll();
        }
        return inventoryRepository.findByTimestampBetween(range.from(), range.to());
    }

    private static <T> List<T> sortByTimestampDesc(List<T> events) {
        return events.stream()
                .sorted(Comparator.comparing(
                        EventQueryService::extractTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private static LocalDateTime extractTimestamp(Object event) {
        if (event instanceof ProductionEvent pe) {
            return pe.getTimestamp();
        }
        if (event instanceof QualityInspectionEvent qe) {
            return qe.getTimestamp();
        }
        if (event instanceof InventoryEvent ie) {
            return ie.getTimestamp();
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
