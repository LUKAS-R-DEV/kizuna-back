package Kizuna_core_service.qualityInspection.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory.service.InventoryService;
import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.recipe.domain.Recipe;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import Kizuna_core_service.productionOrder.service.ProductionOrderService;
import Kizuna_core_service.productionOrder.service.ProductionRealtimePublisher;
import Kizuna_core_service.qualityInspection.domain.QualityInspection;
import Kizuna_core_service.qualityInspection.domain.QualityInspectionStatus;
import Kizuna_core_service.qualityInspection.dto.QualityInspectionRequestDto;
import Kizuna_core_service.qualityInspection.repository.QualityInspectionRepository;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityInspectionServiceTest {

    @Mock
    private QualityInspectionRepository qualityInspectionRepository;

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ProductionOrderService productionOrderService;

    @Mock
    private ProductionRealtimePublisher productionRealtimePublisher;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private QualityInspectionService qualityInspectionService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("Inspector");
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn("inspector-1");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    private ProductionOrder buildWaitingInspectionOrder() {
        Inventory product = new Inventory();
        product.setId(100L);
        product.setName("Produto-X");

        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setName("RECEITA-X");
        recipe.setProduct(product);

        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        order.setStatus(ProductionOrderStatus.WAITING_INSPECTION);
        order.setRecipe(recipe);
        order.setQuantityToProduce(5);
        order.setCreatedBy("planner-1");
        order.setOperatorId("op-1");
        return order;
    }

    @Test
    void create_shouldApproveAndAddStock_whenStatusApproved() {
        ProductionOrder order = buildWaitingInspectionOrder();
        QualityInspectionRequestDto request = new QualityInspectionRequestDto(
                order.getId(),
                QualityInspectionStatus.APPROVED,
                "Tudo ok"
        );

        when(productionOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(qualityInspectionRepository.save(any(QualityInspection.class)))
                .thenAnswer(inv -> {
                    QualityInspection qi = inv.getArgument(0);
                    qi.setId(50L);
                    return qi;
                });

        var response = qualityInspectionService.create(request);

        assertEquals(QualityInspectionStatus.APPROVED, response.status());
        assertEquals(ProductionOrderStatus.APPROVED, order.getStatus());
        verify(inventoryService).addStockProduction(eq(order.getRecipe().getProduct().getId()), eq(5.0), contains("Quality approved"));
        verify(inventoryRepository).save(order.getRecipe().getProduct());
        verify(eventPublisher, atLeastOnce()).publish(any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_shouldRejectAndRegisterWaste_whenStatusRejected() {
        ProductionOrder order = buildWaitingInspectionOrder();
        QualityInspectionRequestDto request = new QualityInspectionRequestDto(
                order.getId(),
                QualityInspectionStatus.REJECTED,
                "Peças fora de medida"
        );

        when(productionOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        qualityInspectionService.create(request);

        assertEquals(ProductionOrderStatus.REJECTED, order.getStatus());
        verify(qualityInspectionRepository).save(any(QualityInspection.class));
        verify(productionOrderRepository).save(order);
        verify(productionOrderService).reorderQueue();
        verify(inventoryService).registerProductionWaste(
                eq(order.getRecipe().getProduct().getId()),
                eq(order.getQuantityToProduce().doubleValue()),
                contains("Rejeição inspeção")
        );
    }

    @Test
    void create_shouldThrowBusinessException_whenReworkAlreadyDoneOnce() {
        ProductionOrder order = buildWaitingInspectionOrder();
        order.setReworkCount(1);

        QualityInspectionRequestDto request = new QualityInspectionRequestDto(
                order.getId(),
                QualityInspectionStatus.REWORK,
                "Retrabalho solicitado"
        );

        when(productionOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> qualityInspectionService.create(request));
        verifyNoInteractions(productionRealtimePublisher);
    }
}

