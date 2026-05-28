package Kizuna_core_service.productionOrder.dto;

import java.time.LocalDateTime;

public record ProductionOrderEventMessage(Long orderId, String recipeName,String type ,LocalDateTime timestamp) {
}
