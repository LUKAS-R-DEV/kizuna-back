package Kizuna_core_service.productionOrder.service;


import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Type;
import Kizuna_core_service.inventory.service.InventoryService;
import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import Kizuna_core_service.recipe.domain.Recipe;
import Kizuna_core_service.recipe.domain.RecipeItem;
import Kizuna_core_service.recipe.repository.RecipeRepository;
import Kizuna_core_service.shared.dto.ApiResponseGeneric;
import Kizuna_core_service.productionOrder.dto.ProductionOrderResponseDto;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.messaging.EventTopics;
import Kizuna_core_service.productionOrder.service.ProductionOrderCalculate;
import Kizuna_core_service.productionOrder.service.ProductionRealtimePublisher;
import Kizuna_core_service.shared.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductionOrderServiceTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private OperatorValidateService operatorValidateService;

    @Mock
    private ProductionOrderCalculate productionOrderCalculate;

    @Mock
    private ProductionRealtimePublisher productionRealtimePublisher;

    @InjectMocks
    private ProductionOrderService productionOrderService;

    // Mock estático para SecurityUtils (necessário se SecurityUtils for uma classe com métodos estáticos)
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }


    @Test
    void pauseProductionOrder() {
        Long orderId = 1L;
        String userId = "user123";

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Bolo de Chocolate");

        ProductionOrder order = new ProductionOrder();
        order.setId(orderId);
        order.setStatus(ProductionOrderStatus.IN_PROGRESS);
        order.setOperatorId(userId);
        order.setRecipe(recipe);
        order.setQuantityToProduce(1);


        when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productionOrderRepository.findByStatus(ProductionOrderStatus.PLANNED))
                .thenReturn(new java.util.ArrayList<>(java.util.List.of(order)));
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(userId);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("Joao");

        // Act
        ApiResponseGeneric response = productionOrderService.pause(orderId);

        // Assert
        assertEquals(ProductionOrderStatus.PAUSED, order.getStatus());
        verify(productionOrderRepository).save(order);
        verify(eventPublisher, atLeast(3)).publish(any(), any(), any(), any(), any(), any());
      ;

    }

    @Test
    void start_shouldConsumeInventoryAndSetInProgress_whenPlannedAndStockSufficient() {
        Long orderId = 1L;

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setName("AÇO");
        inventory.setType(Type.RAW);
        inventory.setQuantity(100.0);

        RecipeItem item = new RecipeItem();
        item.setInventory(inventory);
        item.setQuantity(2.0); // por unidade

        Recipe recipe = new Recipe();
        recipe.setId(5L);
        recipe.setName("PEÇA-X");
        java.util.Set<RecipeItem> items = new java.util.HashSet<>();
        items.add(item);
        recipe.setItems(items);

        ProductionOrder order = new ProductionOrder();
        order.setId(orderId);
        order.setStatus(ProductionOrderStatus.PLANNED);
        order.setRecipe(recipe);
        order.setQuantityToProduce(10);
        order.setOperatorId("op-1");

        when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        ProductionOrder planned = new ProductionOrder();
        planned.setId(99L);
        planned.setRecipe(recipe);
        planned.setStatus(ProductionOrderStatus.PLANNED);
        when(productionOrderRepository.findByStatus(ProductionOrderStatus.PLANNED))
                .thenReturn(new java.util.ArrayList<>(java.util.List.of(planned)));
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn("planner-1");
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("Planner");

        ProductionOrderResponseDto response = productionOrderService.start(orderId);

        assertEquals(ProductionOrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals(orderId, response.id());
        verify(inventoryService).consume(eq(inventory.getId()), eq(20.0), contains("Production order ID"));
        verify(productionOrderRepository).save(order);
        verify(eventPublisher, atLeastOnce()).publish(eq(EventTopics.PRODUCTION_STARTED), any(), any(), any(), any(), any());
        verify(productionRealtimePublisher).publishOrder(order);
    }

    @Test
    void start_shouldThrowBusinessException_whenStatusIsNotPlannedOrPaused() {
        Long orderId = 2L;
        ProductionOrder order = new ProductionOrder();
        order.setId(orderId);
        order.setStatus(ProductionOrderStatus.IN_PROGRESS);

        when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> productionOrderService.start(orderId));
        verifyNoInteractions(inventoryService);
    }

    @Test
    void finish_shouldMoveToWaitingInspectionAndPublishEvents() {
        Long orderId = 3L;
        Recipe recipe = new Recipe();
        recipe.setName("PROD-A");

        ProductionOrder order = new ProductionOrder();
        order.setId(orderId);
        order.setStatus(ProductionOrderStatus.IN_PROGRESS);
        order.setRecipe(recipe);
        order.setQuantityToProduce(5);
        order.setLastStartTime(java.time.LocalDateTime.now().minusMinutes(10));

        when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn("op-1");
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("Operador");

        ProductionOrderResponseDto response = productionOrderService.finish(orderId);

        assertEquals(ProductionOrderStatus.WAITING_INSPECTION, order.getStatus());
        assertEquals(orderId, response.id());
        verify(productionOrderRepository).save(order);
        verify(productionRealtimePublisher).publishOrder(order);
        verify(eventPublisher, atLeastOnce()).publish(eq(EventTopics.PRODUCTION_FINISHED), any(), any(), any(), any(), any());
    }
}
