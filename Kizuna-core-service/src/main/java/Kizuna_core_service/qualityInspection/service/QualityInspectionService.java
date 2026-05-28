package Kizuna_core_service.qualityInspection.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory.service.InventoryService;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.repository.InventoryMovementRepository;
import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import Kizuna_core_service.productionOrder.service.ProductionOrderService;
import Kizuna_core_service.productionOrder.service.ProductionRealtimePublisher;
import Kizuna_core_service.qualityInspection.domain.QualityInspection;
import Kizuna_core_service.qualityInspection.domain.QualityInspectionStatus;
import Kizuna_core_service.qualityInspection.dto.QualityInspectionRequestDto;
import Kizuna_core_service.qualityInspection.dto.QualityInspectionResponseDto;
import Kizuna_core_service.qualityInspection.repository.QualityInspectionRepository;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.messaging.EventTopics;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import Kizuna_core_service.shared.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class QualityInspectionService {
    private final QualityInspectionRepository qualityInspectionRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final ProductionOrderService productionOrderService;
    private final ProductionRealtimePublisher productionRealtimePublisher;
    private final EventPublisher eventPublisher;


    public List<QualityInspectionResponseDto> findAll(){
        return qualityInspectionRepository.findAll().stream().map(this::qualityInspectionResponseDto).toList();
    }
    public QualityInspectionResponseDto findById(Long id){
        QualityInspection qualityInspection = qualityInspectionRepository.findById(id).orElseThrow(() -> new NotFoundException("Quality inspection not found"));
        return qualityInspectionResponseDto(qualityInspection);
    }

    @Transactional
    public QualityInspectionResponseDto create(QualityInspectionRequestDto qualityInspectionRequestDto){
        ProductionOrder productionOrder=productionOrderRepository.findById(qualityInspectionRequestDto.productionOrderId()).orElseThrow(()-> new NotFoundException("Quality inspection not found"));

        if(!productionOrder.getStatus().equals(ProductionOrderStatus.WAITING_INSPECTION)){
            throw new BusinessException("Production order is not in waiting inspection status");
        }

        QualityInspection qualityInspection=new QualityInspection();
        qualityInspection.setInspectedBy(SecurityUtils.getUsername());
        qualityInspection.setNotes(qualityInspectionRequestDto.notes());
        qualityInspection.setStatus(qualityInspectionRequestDto.status());
        qualityInspection.setProductionOrder(productionOrder);
        qualityInspection.setCreatedAt(LocalDateTime.now());

        if(qualityInspection.getStatus().equals(QualityInspectionStatus.REJECTED)){
            productionOrder.setStatus(ProductionOrderStatus.REJECTED);
            qualityInspectionRepository.save(qualityInspection);
            productionOrderRepository.save(productionOrder);
            productionOrderService.reorderQueue();
            String productName = productionOrder.getRecipe().getProduct().getName();
            inventoryService.registerProductionWaste(
                    productionOrder.getRecipe().getProduct().getId(),
                    productionOrder.getQuantityToProduce().doubleValue(),
                    String.format("Rejeição inspeção PO-%d — %s", productionOrder.getId(), productName)
            );
            eventPublisher.publish(EventTopics.INSPECTION_REJECTED, "QUALITY_INSPECTION", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("result", "REJECTED", "recipeName", productionOrder.getRecipe().getName()));
            eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "INSPECTION_REJECTED", "result", "REJECTED", "recipeName", productionOrder.getRecipe().getName()));
            eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getCreatedBy(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.rejected", "title", "Ordem de Produção Rejeitada", "message", "A ordem de produção foi rejeitada: " + productionOrder.getRecipe().getName(),"targetRole", "PLANNER"));
            eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.rejected", "title", "Ordem de Produção Rejeitada", "message", "A ordem de produção foi rejeitada: " + productionOrder.getRecipe().getName(),"userId", productionOrder.getOperatorId()));
            return qualityInspectionResponseDto(qualityInspection);

        }
        if(qualityInspection.getStatus().equals(QualityInspectionStatus.REWORK)){
            if(productionOrder.getReworkCount() == 1){
                throw new BusinessException("The production order has already undergone rework; accept or reject it.");
            }
            productionOrder.setStatus(ProductionOrderStatus.REWORK);
            qualityInspection.setNotes(qualityInspectionRequestDto.notes());
            productionOrder.setWorkedMinutes(0L);
            productionOrder.setStartTime(null);
            productionOrder.setLastStartTime(null);
            productionOrder.setEndTime(null);
            qualityInspectionRepository.save(qualityInspection);
            productionOrderRepository.save(productionOrder);
            productionRealtimePublisher.publishOrder(productionOrder);
            eventPublisher.publish(EventTopics.INSPECTION_REWORK, "QUALITY_INSPECTION", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("result", "REWORK", "recipeName", productionOrder.getRecipe().getName()));
            eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "INSPECTION_REWORK", "result", "REWORK", "recipeName", productionOrder.getRecipe().getName()));
            eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getCreatedBy(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.rework", "title", "retrabalho necessario", "message", "A ordem de produção deve ser retrabalhada: " + productionOrder.getRecipe().getName(),"targetRole", "PLANNER"));
            eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.rework", "title", "retrabalho necessario", "message", "A ordem de produção deve ser retrabalhada: " + productionOrder.getRecipe().getName(),"userId", productionOrder.getOperatorId()));
            return qualityInspectionResponseDto(qualityInspection);
        }

        Inventory inventory=productionOrder.getRecipe().getProduct();
        Double quantityProduced = productionOrder.getQuantityToProduce().doubleValue();
        String reason = "Quality approved - ProductionOrder ID: " + productionOrder.getId();
        inventoryService.addStockProduction(inventory.getId(), quantityProduced, reason);
        productionOrder.setStatus(ProductionOrderStatus.APPROVED);
        inventory.updateStatus();
        qualityInspectionRepository.save(qualityInspection);
        productionOrderRepository.save(productionOrder);
        inventoryRepository.save(inventory);
        eventPublisher.publish(EventTopics.INSPECTION_APPROVED, "QUALITY_INSPECTION", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("result", "APPROVED", "recipeName", productionOrder.getRecipe().getName()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "INSPECTION_APPROVED", "result", "APPROVED", "recipeName", productionOrder.getRecipe().getName()));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getCreatedBy(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.approved", "title", "Ordem de Produção Aprovada", "message", "A ordem de produção foi aprovada: " + productionOrder.getRecipe().getName(),"targetRole", "PLANNER"));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.approved", "title", "Ordem de Produção Aprovada", "message", "A ordem de produção foi aprovada: " + productionOrder.getRecipe().getName(),"userId", productionOrder.getOperatorId()));
        return qualityInspectionResponseDto(qualityInspection);
    }
    private QualityInspectionResponseDto qualityInspectionResponseDto(QualityInspection qualityInspection){
        return new QualityInspectionResponseDto(qualityInspection.getId(),qualityInspection.getProductionOrder().getRecipe().getName(),qualityInspection.getStatus(),qualityInspection.getNotes(),qualityInspection.getInspectedBy(),qualityInspection.getCreatedAt());
    }





}
