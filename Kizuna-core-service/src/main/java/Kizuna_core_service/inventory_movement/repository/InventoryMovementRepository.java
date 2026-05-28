package Kizuna_core_service.inventory_movement.repository;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
}
