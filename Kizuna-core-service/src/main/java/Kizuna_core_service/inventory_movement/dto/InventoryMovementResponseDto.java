package Kizuna_core_service.inventory_movement.dto;

import Kizuna_core_service.inventory_movement.domain.MovementType;

public record InventoryMovementResponseDto(
        Long id,
        Long inventoryId,
        String inventoryName,
        String reason,
        Double quantity,
        String createdAt,
        MovementType type,
        String updatedAt) {
}
