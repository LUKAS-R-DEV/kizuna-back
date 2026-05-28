package Kizuna_core_service.productionOrder.dto;

import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record ProductionOrderResponseDto(Long id,String recipeName,String createdBy,String operatorName,String operatorId,Integer quantityToProduce, LocalDateTime startTime,Integer priority,LocalDateTime deadline,Integer queuePosition,LocalDateTime endTime, ProductionOrderStatus status,Long estimatedTotalTime,Double progress,LocalDateTime eta,Long remainingTime,ProductionOrderStatus calculatedStatus) {

}
