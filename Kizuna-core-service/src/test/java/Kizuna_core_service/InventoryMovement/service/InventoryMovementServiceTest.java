package Kizuna_core_service.InventoryMovement.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Type;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.repository.InventoryMovementRepository;
import Kizuna_core_service.inventory_movement.service.InventoryMovementService;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.messaging.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventPublisher eventPublisher;


    @InjectMocks
    private InventoryMovementService inventoryMovementService;


    @Test
    void shouldMoveNotSuccessfully() {
        Inventory inventory=new Inventory();
        inventory.setId(1L);
        inventory.setType(Type.RAW);
        inventory.setQuantity(10.0);
        inventory.setActive(true);


        when(inventoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(inventory));

        assertThrows(BusinessException.class, () -> {
            inventoryMovementService.inventoryMovement(inventory.getId(), 0.0, MovementType.EXIT, "test");
        });
        verify(inventoryMovementRepository,never()).save(any());


    }
    @Test
    void shouldMoveSuccessfully() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setQuantity(10.0);
        inventory.setActive(true);

        // Criamos a movimentação que esperamos que o banco "gere"
        InventoryMovement savedMovement = new InventoryMovement();
        savedMovement.setId(100L); // O ID que evitará o NullPointerException

        // Configuramos os Mocks
        when(inventoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(savedMovement);

        inventoryMovementService.inventoryMovement(1L, 5.0, MovementType.EXIT, "test");


        verify(inventoryMovementRepository).save(any());
    }

    }




