package Kizuna_core_service.recipe.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecipeItemRequestDto(
        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be positive")
        Double quantity,
        @NotNull(message = "Inventory ID cannot be null")
        Long inventoryId) {
}
