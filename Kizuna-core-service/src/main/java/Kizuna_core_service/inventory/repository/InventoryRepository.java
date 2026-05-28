package Kizuna_core_service.inventory.repository;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByActiveTrue();
    List<Inventory> findAllByType(String type);
    Optional<Inventory> findByType(Type type);
    Optional<Inventory> findByIdAndActiveTrue(Long id);

}
