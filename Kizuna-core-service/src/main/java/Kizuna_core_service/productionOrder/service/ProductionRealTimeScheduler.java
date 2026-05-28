package Kizuna_core_service.productionOrder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductionRealTimeScheduler {

    private final ProductionRealtimePublisher productionRealtimePublisher;

    /** Broadcast progress/remaining time every minute (only ticks while IN_PROGRESS). */
    @Scheduled(fixedRate = 60_000)
    public void publishProductionProgress() {
        productionRealtimePublisher.publishAllTrackedOrders();
    }
}
