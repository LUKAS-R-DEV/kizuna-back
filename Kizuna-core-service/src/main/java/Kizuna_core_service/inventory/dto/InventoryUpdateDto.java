package Kizuna_core_service.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record InventoryUpdateDto(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "category is required")
        String category,
        @NotBlank(message = "location is required")
        String location)
{
}
