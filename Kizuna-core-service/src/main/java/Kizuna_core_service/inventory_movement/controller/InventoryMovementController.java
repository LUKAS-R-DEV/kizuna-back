package Kizuna_core_service.inventory_movement.controller;

import Kizuna_core_service.inventory_movement.dto.InventoryMovementResponseDto;
import Kizuna_core_service.inventory_movement.service.InventoryMovementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory-movement")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;

    public InventoryMovementController(InventoryMovementService inventoryMovementService) {
        this.inventoryMovementService = inventoryMovementService;
    }

    @GetMapping
    public ResponseEntity<Page<InventoryMovementResponseDto>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(inventoryMovementService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryMovementResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryMovementService.findById(id));
    }
}
