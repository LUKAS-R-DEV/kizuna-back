package Kizuna_core_service.productionOrder.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProductionOrderScheduler {
    private final ProductionOrderService productionOrderService;
    public ProductionOrderScheduler(ProductionOrderService productionOrderService) {
        this.productionOrderService = productionOrderService;
    }
    @Scheduled(fixedRate = 60000)
    public void checkOrders() {
        productionOrderService.autoFinishByTime();
    }

}
