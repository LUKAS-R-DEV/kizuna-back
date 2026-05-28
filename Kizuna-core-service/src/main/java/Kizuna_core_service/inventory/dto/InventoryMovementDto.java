package Kizuna_core_service.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record InventoryMovementDto(
        @NotNull (message = "Inventory id is required")
        Long inventoryId,
        @Min(value = 1, message = "Quantity must be greater than 0")
        Double quantity,
        @NotBlank
        String reason) {

}
