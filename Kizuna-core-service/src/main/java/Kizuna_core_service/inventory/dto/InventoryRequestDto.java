package Kizuna_core_service.inventory.dto;

import Kizuna_core_service.inventory.domain.Type;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryRequestDto(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "category is required")
        String category,
        @NotBlank(message = "location is required")
        String location,
        @NotNull(message = "quantity is required")
        @PositiveOrZero(message = "quantity must be positive or zero")
        Double quantity,
        @NotNull(message = "minStock is required")
        @PositiveOrZero(message = "minStock must be positive or zero")
        Double minStock,
        @NotNull(message = "type is required")
        Type type,
        @NotNull(message = "supplier is required")
        String supplier ) {
}
