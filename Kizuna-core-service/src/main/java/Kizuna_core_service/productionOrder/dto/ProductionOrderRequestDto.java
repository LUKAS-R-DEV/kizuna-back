package Kizuna_core_service.productionOrder.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProductionOrderRequestDto(
        @NotNull (message = "Recipe id cannot be null")
        Long recipeId ,
        @NotNull (message = "Quantity to produce cannot be null")
        Integer quantityToProduce,
        @NotNull (message = "Priority cannot be null")
        Integer priority,
        @NotNull (message = "Operator id cannot be null")
        String operatorId,
        LocalDateTime deadline) {

}
