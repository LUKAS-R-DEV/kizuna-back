package Kizuna_core_service.productionOrder.service;

import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductionRealtimePublisher {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderCalculate productionOrderCalculate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void publishOrder(ProductionOrder order) {
        if (order == null) {
            return;
        }
        simpMessagingTemplate.convertAndSend("/topic/production", (Object) buildPayload(order));
    }

    public void publishAllTrackedOrders() {
        List<ProductionOrderStatus> tracked = List.of(
                ProductionOrderStatus.IN_PROGRESS,
                ProductionOrderStatus.REWORK,
                ProductionOrderStatus.PAUSED
        );
        productionOrderRepository.findByStatusIn(tracked).forEach(this::publishOrder);
    }

    private Map<String, Object> buildPayload(ProductionOrder order) {
        LocalDateTime eta = productionOrderCalculate.calculateETA(order);
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", order.getId());
        payload.put("recipeName", order.getRecipe().getName());
        payload.put("status", order.getStatus().name());
        payload.put("progress", productionOrderCalculate.calculateProgress(order));
        payload.put("remainingTime", productionOrderCalculate.calculateRemainingTime(order));
        payload.put("eta", eta != null ? eta.toString() : null);
        payload.put("operatorName", order.getOperatorName());
        return payload;
    }
}
