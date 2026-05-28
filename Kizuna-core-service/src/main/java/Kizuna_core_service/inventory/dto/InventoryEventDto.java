package Kizuna_core_service.inventory.dto;

import java.time.LocalDateTime;

public record InventoryEventDto(String productName, String type, double quantity, LocalDateTime timestamp) {
}
