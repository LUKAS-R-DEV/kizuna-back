package Kizuna_core_service.inventory.dto;

import Kizuna_core_service.inventory.domain.Status;
import Kizuna_core_service.inventory.domain.Type;
import lombok.Builder;

@Builder
public record InventoryResponseDto(Long id, String name, String category,Type type,String location, Double quantity, Double minStock, String supplier, Status status, Boolean active) {
}
