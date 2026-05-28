package com.kizuna.data_service.metrics.service;

import com.kizuna.data_service.domain.ProductionEvent;
import com.kizuna.data_service.domain.QualityInspectionEvent;
import com.kizuna.data_service.metrics.dtos.OeeMetricsDto;
import com.kizuna.data_service.metrics.dtos.ProductionMetricsDto;
import com.kizuna.data_service.metrics.dtos.QualityMetricsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EventQueryService eventQueryService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getProductionMetrics_countsStatusesInPeriod() {
        LocalDateTime now = LocalDateTime.now();
        when(eventQueryService.productionEvents(any(), any(), any(), eq("ALL")))
                .thenReturn(List.of(
                        event(1L, "PLANNED", now),
                        event(2L, "START", now),
                        event(3L, "FINISH", now),
                        event(4L, "WAITING_INSPECTION", now)
                ));

        ProductionMetricsDto metrics = dashboardService.getProductionMetrics("7d", null, null);

        assertEquals(4L, metrics.totalOrders());
        assertEquals(1L, metrics.StartedOrders());
        assertEquals(2L, metrics.finishedOrders());
        assertEquals(1L, metrics.plannedOrders());
        assertEquals(50.0, metrics.efficiencyPercent());
    }

    @Test
    void getQualityMetrics_calculatesRates() {
        LocalDateTime now = LocalDateTime.now();
        when(eventQueryService.qualityEvents(any(), any(), any(), eq("ALL")))
                .thenReturn(List.of(
                        inspection("APPROVED", now),
                        inspection("APPROVED", now),
                        inspection("REJECTED", now),
                        inspection("REWORK", now)
                ));

        QualityMetricsDto metrics = dashboardService.getQualityMetrics("30d", null, null);

        assertEquals(2L, metrics.aprovedOrders());
        assertEquals(1L, metrics.rejectedOrders());
        assertEquals(1L, metrics.reworkOrders());
        assertEquals(25.0, metrics.rejectionRatePercent());
        assertEquals(50.0, metrics.yieldPercent());
    }

    @Test
    void getOeeMetrics_combinesAvailabilityAndQuality() {
        LocalDateTime now = LocalDateTime.now();
        when(eventQueryService.productionEvents(any(), any(), any(), eq("ALL")))
                .thenReturn(List.of(
                        event(1L, "FINISH", now),
                        event(2L, "PLANNED", now)
                ));
        when(eventQueryService.qualityEvents(any(), any(), any(), eq("ALL")))
                .thenReturn(List.of(
                        inspection("APPROVED", now),
                        inspection("REJECTED", now)
                ));

        OeeMetricsDto oee = dashboardService.getOeeMetrics("7d", null, null);

        assertEquals(50.0, oee.availabilityPercent());
        assertEquals(50.0, oee.qualityPercent());
        assertEquals(25.0, oee.oeePercent());
    }

    private static ProductionEvent event(Long orderId, String status, LocalDateTime timestamp) {
        ProductionEvent event = new ProductionEvent();
        event.setOrderId(orderId);
        event.setStatus(status);
        event.setTimestamp(timestamp);
        return event;
    }

    private static QualityInspectionEvent inspection(String result, LocalDateTime timestamp) {
        return QualityInspectionEvent.builder()
                .result(result)
                .timestamp(timestamp)
                .build();
    }
}
