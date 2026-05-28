package Kizuna_core_service.inventory_movement.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.dto.InventoryMovementResponseDto;
import Kizuna_core_service.inventory_movement.repository.InventoryMovementRepository;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.messaging.EventTopics;
import Kizuna_core_service.shared.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@AllArgsConstructor
@Service
public class InventoryMovementService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final EventPublisher eventPublisher;

        public Page<InventoryMovementResponseDto> findAll(Pageable pageable) {
            return inventoryMovementRepository.findAll(pageable).map(this::toResponseInventoryMovement);
        }

        public InventoryMovementResponseDto findById(Long id){
            InventoryMovement inventoryMovement=inventoryMovementRepository.findById(id).orElseThrow(() -> new NotFoundException("InventoryMovement not found"));

            return toResponseInventoryMovement(inventoryMovement);
        }

        @Transactional
        public void inventoryMovement(Long inventoryId,double quantity,MovementType type,String reason){
            Inventory inventory=inventoryRepository.findByIdAndActiveTrue(inventoryId).orElseThrow(() -> new NotFoundException("Inventory not found"));
            if(quantity<=0){
                throw new BusinessException("Quantity must be greater than 0");
            }

            InventoryMovement inventoryMovement=InventoryMovement.builder().
                    inventory(inventory).
                    quantity(quantity).
                    type(type).
                    reason(reason).
                    build();

            InventoryMovement savedMovement = inventoryMovementRepository.save(inventoryMovement);
            eventPublisher.publish(EventTopics.AUDIT,"INVENTORY_MOVEMENT",savedMovement.getId().toString(),SecurityUtils.getUserId(),SecurityUtils.getUsername(),Map.of("action", "MOVEMENT", "inventoryId", inventory.getId(), "movementType", type.name(), "quantity", quantity));
            eventPublisher.publish(EventTopics.INVENTORY_MOVEMENT,"INVENTORY_MOVEMENT",savedMovement.getId().toString(),SecurityUtils.getUserId(),SecurityUtils.getUsername(),Map.of("inventoryId", inventory.getId(), "movementType", type.name(), "quantity", quantity));

        }

        private InventoryMovementResponseDto toResponseInventoryMovement(InventoryMovement inventoryMovement) {
            return new InventoryMovementResponseDto(
                    inventoryMovement.getId(),
                    inventoryMovement.getInventory().getId(),
                    inventoryMovement.getInventory().getName(),
                    inventoryMovement.getReason(),
                    inventoryMovement.getQuantity(),
                    toUtcIso(inventoryMovement.getCreatedAt()),
                    inventoryMovement.getType(),
                    toUtcIso(inventoryMovement.getUpdatedAt()));
        }

        private static String toUtcIso(LocalDateTime value) {
            if (value == null) {
                return null;
            }
            return value.atZone(ZoneOffset.UTC).toInstant().toString();
        }
}
