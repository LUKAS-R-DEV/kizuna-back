package com.kizuna.data_service.integration.read;

import com.kizuna.data_service.integration.config.IntegrationProperties;
import com.kizuna.data_service.metrics.dtos.InventoryMetricsDto;
import com.kizuna.data_service.metrics.dtos.OeeMetricsDto;
import com.kizuna.data_service.metrics.dtos.ProductionMetricsDto;
import com.kizuna.data_service.metrics.dtos.QualityMetricsDto;
import com.kizuna.data_service.metrics.service.DashboardService;
import com.kizuna.data_service.metrics.service.EventQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationReadServiceTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private EventQueryService eventQueryService;

    @Mock
    private IntegrationProperties properties;

    @InjectMocks
    private IntegrationReadService integrationReadService;

    @Test
    void metricsSummaryMapsDashboardValues() {
        when(dashboardService.getProductionMetrics(null, null, "30d"))
                .thenReturn(new ProductionMetricsDto(
                        10L, 3L, 5L, 1L, 0L, 0L, 1L, 50.0,
                        "30d", LocalDateTime.now().minusDays(30), LocalDateTime.now()));
        when(dashboardService.getInventoryMetrics(null, null, "30d"))
                .thenReturn(new InventoryMetricsDto(20L, 4L, 16L, "30d", null, null));
        when(dashboardService.getQualityMetrics(null, null, "30d"))
                .thenReturn(new QualityMetricsDto(
                        8L, 2L, 1L, 20.0, 80.0, "30d", null, null));

        var summary = integrationReadService.metricsSummary(null, null, "30d");

        assertThat(summary.production().totalOrders()).isEqualTo(10L);
        assertThat(summary.inventory().lowStockMovements()).isEqualTo(4L);
        assertThat(summary.quality().approved()).isEqualTo(8L);
    }

    @Test
    void oeeMapsDashboardValues() {
        when(dashboardService.getOeeMetrics("7d", null, null))
                .thenReturn(new OeeMetricsDto(
                        80.0, 90.0, 72.0, 8L, 10L, 7L, 8L,
                        "7d", LocalDateTime.now().minusDays(7), LocalDateTime.now()));

        var oee = integrationReadService.oee("7d", null, null);

        assertThat(oee.oeePercent()).isEqualTo(72.0);
        assertThat(oee.completedOrders()).isEqualTo(8L);
    }

    @Test
    void productionOrdersRespectLimit() {
        when(properties.maxPageSize()).thenReturn(10);
        when(eventQueryService.productionEvents(null, null, "30d", "ALL"))
                .thenReturn(List.of());

        var orders = integrationReadService.productionOrders(null, null, "30d", "ALL", 5);

        assertThat(orders).isEmpty();
    }
}
