package Kizuna_core_service.inventory.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Status;
import Kizuna_core_service.inventory.domain.Type;
import Kizuna_core_service.inventory.dto.InventoryMovementDto;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.service.InventoryMovementService;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldThrowExceptionWhenConsumingMoreThanAvailable() {

        // 🔹 Arrange (preparar)
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setName("ITEM-1");
        inventory.setQuantity(10.0);
        inventory.setStatus(Status.GOOD);

        when(inventoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(inventory));

        // 🔹 Act + Assert
        assertThrows(BusinessException.class, () -> {
            inventoryService.consume(1L, 20.0,"test");
        });

        // 🔹 Verifica que NÃO salvou
        verify(inventoryRepository, never()).save(any());
    }
    @Test
    void shouldConsumeSuccessfully() {
            // 1. Arrange
            Inventory inventory = new Inventory();
            inventory.setId(1L);
            inventory.setType(Type.RAW);
            inventory.setName("ITEM-RAW");
            inventory.setQuantity(10.0);
            inventory.setActive(true);
            inventory.setStatus(Status.GOOD);

        doReturn(Optional.of(inventory)).when(inventoryRepository).findByIdAndActiveTrue(1L);

            inventoryService.consume(1L, 5.0, "test");

            verify(inventoryRepository).save(inventory);


            // Opcional: verificar se o movimento foi chamado
        verify(inventoryMovementService).inventoryMovement(
                anyLong(),
                anyDouble(),
                any(),
                anyString()
        );
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {

        doReturn(Optional.empty())
                .when(inventoryRepository).findByIdAndActiveTrue(1L);


        assertThrows(NotFoundException.class, () -> {
            inventoryService.consume(1L, 5.0, "test");
        });

        verify(inventoryRepository, never()).save(any());
        verify(inventoryMovementService, never()).inventoryMovement(anyLong(), anyDouble(), any(), anyString());
    }
    @Test
    void shouldConsumeAllQuantity() {

        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setType(Type.RAW);
        inventory.setName("ITEM-RAW");
        inventory.setQuantity(10.0);
        inventory.setStatus(Status.GOOD);

        when(inventoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(inventory));

        inventoryService.consume(1L, 10.0, "test");

        assert inventory.getQuantity() == 0.0;

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void entryMovement() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setType(Type.RAW);
        inventory.setName("ITEM-RAW");
        inventory.setQuantity(10.0);
        inventory.setStatus(Status.GOOD);

        InventoryMovementDto inventoryMovementDto = new InventoryMovementDto(1L, 1.0, "test");

        // CORREÇÃO: O mock deve retornar o inventário para que o service consiga somar
        // Sintaxe correta: doReturn(valor).when(mock).metodoChamado(argumento)
        doReturn(Optional.of(inventory))
                .when(inventoryRepository)
                .findByIdAndActiveTrue(1L);

        // 2. Execução (When)
        inventoryService.entryInventory(inventoryMovementDto);

        // 3. Verificações (Then)
        // Use assertEquals do JUnit para garantir que o teste falhe se o valor estiver errado
        Assertions.assertEquals(11.0, inventory.getQuantity(), "A quantidade deveria ser 10 + 5 = 15");

        // Verifica se o save foi chamado para persistir a alteração
        verify(inventoryRepository).save(inventory);

    }

    @Test
    void exitMovement() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setType(Type.RAW);
        inventory.setName("ITEM-RAW");
        inventory.setQuantity(10.0);
        inventory.setStatus(Status.GOOD);

        doReturn(Optional.of(inventory))
                .when(inventoryRepository)
                .findByIdAndActiveTrue(1L);



        InventoryMovementDto inventoryMovementDto=new InventoryMovementDto(inventory.getId(),2.0,"test");

        inventoryService.exitInventory(inventoryMovementDto);

        Assertions.assertEquals(8.0, inventory.getQuantity(), "A quantidade deveria ser 10 - 2 = 8");

        verify(inventoryRepository).save(inventory);

    }




}