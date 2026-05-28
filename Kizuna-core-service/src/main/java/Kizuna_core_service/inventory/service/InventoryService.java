package Kizuna_core_service.inventory.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Status;
import Kizuna_core_service.inventory.domain.Type;
import Kizuna_core_service.inventory.dto.InventoryMovementDto;
import Kizuna_core_service.inventory.dto.InventoryRequestDto;
import Kizuna_core_service.inventory.dto.InventoryResponseDto;
import Kizuna_core_service.inventory.dto.InventoryUpdateDto;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.service.InventoryMovementService;
import Kizuna_core_service.shared.dto.ApiResponseGeneric;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.messaging.EventTopics;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import Kizuna_core_service.shared.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final EventPublisher eventPublisher;
    private final InventoryMovementService inventoryMovementService;

    public List<InventoryResponseDto> findAll(){
        return inventoryRepository.findAll().stream().map(this::toResponseDto).toList();
    }
    public List<InventoryResponseDto> findByType(String type){
        return inventoryRepository.findAllByType(type).stream().map(this::toResponseDto).toList();
    }
    public InventoryResponseDto findById(Long id){
       Inventory inventory = inventoryRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new NotFoundException("Inventory not found"));
       return toResponseDto(inventory);
    }
    @Transactional
    public InventoryResponseDto create(InventoryRequestDto requestDto){
        Inventory inventory=new Inventory();
        inventory.setName(requestDto.name());
        inventory.setQuantity(requestDto.quantity());
        if(inventory.getQuantity()<0){
            throw new BusinessException("Quantity cannot be negative");
        }
        inventory.setMinStock(requestDto.minStock());
        if(inventory.getMinStock()<0){
            throw new BusinessException("Min stock cannot be negative");
        }
        inventory.setSupplier(requestDto.supplier());

        inventory.setCategory(requestDto.category());
        inventory.setLocation(requestDto.location());
        inventory.setActive(true);
        inventory.setType(requestDto.type());
        inventory.updateStatus();
        inventoryRepository.save(inventory);
        eventPublisher.publish(EventTopics.INVENTORY_CREATED, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("inventoryName", inventory.getName(), "action", "CREATE", "quantity", inventory.getQuantity(),"status",inventory.getStatus().name()));
        eventPublisher.publish(EventTopics.AUDIT,"INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "CREATE", "inventoryId", inventory.getId(), "quantity", inventory.getQuantity()));
        return toResponseDto(inventory);
    }

@Transactional
    public InventoryResponseDto update(Long id, InventoryUpdateDto updateDto){
        Inventory inventory=inventoryRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new NotFoundException("Inventory not found"));
        inventory.setName(updateDto.name());
        inventory.setCategory(updateDto.category());
        inventory.setLocation(updateDto.location());
        inventoryRepository.save(inventory);
        eventPublisher.publish(EventTopics.INVENTORY_UPDATED, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("inventoryName", inventory.getName(), "action", "UPDATE", "quantity", inventory.getQuantity(),"status",inventory.getStatus().name()));
        eventPublisher.publish(EventTopics.AUDIT, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "UPDATE", "inventoryId", inventory.getId(), "quantity", inventory.getQuantity()));
        return toResponseDto(inventory);
    }

@Transactional
    public InventoryResponseDto disable(Long id){
        Inventory inventory=inventoryRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new NotFoundException("Inventory not found"));
        inventory.setActive(false);
        inventoryRepository.save(inventory);
        eventPublisher.publish(EventTopics.INVENTORY_DISABLE, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("inventoryName", inventory.getName(), "action", "DISABLE", "quantity", inventory.getQuantity(),"active",inventory.getActive()));
        eventPublisher.publish(EventTopics.AUDIT, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "DISABLE", "inventoryId", inventory.getId()));
        return toResponseDto(inventory);
    }

    @Transactional
    public InventoryResponseDto enable(Long id){
        Inventory inventory=inventoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Inventory not found"));
        inventory.setActive(true);
        inventoryRepository.save(inventory);
        eventPublisher.publish(EventTopics.INVENTORY, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("inventoryName", inventory.getName(), "action", "ENABLE", "quantity", inventory.getQuantity(),"active",inventory.getActive()));
        eventPublisher.publish(EventTopics.AUDIT, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "ENABLE", "inventoryId", inventory.getId()));
        return toResponseDto(inventory);
    }

    @Transactional

    public ApiResponseGeneric entryInventory(InventoryMovementDto inventoryMovementDto){
        Inventory inventory=inventoryRepository.findByIdAndActiveTrue(inventoryMovementDto.inventoryId()).orElseThrow(() -> new NotFoundException("Inventory not found"));

        double quantityMovement=inventoryMovementDto.quantity();

        if(inventoryMovementDto.quantity()==0){
            throw new BusinessException("Quantity must be greater than 0");
        }


        inventory.setQuantity(inventory.getQuantity()+quantityMovement);


        inventoryMovementService.inventoryMovement(inventory.getId(), quantityMovement, MovementType.ENTRY, inventoryMovementDto.reason());
        inventoryRepository.save(inventory);
        eventPublisher.publish(EventTopics.INVENTORY_ENTRY, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("inventoryName", inventory.getName(), "action", "ENTRY", "quantity", inventory.getQuantity(),"status",inventory.getStatus().name()));
        eventPublisher.publish(EventTopics.AUDIT, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "ENTRY", "inventoryId", inventory.getId(), "quantity", inventory.getQuantity()));
        return apiResponseGeneric("Inventory entry movement registered successfully");

    }
    public ApiResponseGeneric exitInventory(InventoryMovementDto inventoryMovementDto){
        Inventory inventory=inventoryRepository.findByIdAndActiveTrue(inventoryMovementDto.inventoryId()).orElseThrow(() -> new NotFoundException("Inventory not found"));

        double quantityMovement=inventoryMovementDto.quantity();
        if(inventory.getQuantity()<quantityMovement){
            throw new BusinessException("Not enough inventory");
        }

        if(inventoryMovementDto.quantity()==0){
            throw new BusinessException("Quantity must be greater than 0");
        }

        inventory.setQuantity(inventory.getQuantity()-quantityMovement);

        inventoryMovementService.inventoryMovement(inventory.getId(), quantityMovement, MovementType.EXIT, inventoryMovementDto.reason());
        inventoryRepository.save(inventory);
        eventPublisher.publish(EventTopics.INVENTORY_EXIT, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("inventoryName", inventory.getName(), "action", "EXIT", "quantity", inventory.getQuantity(),"status",inventory.getStatus().name()));
        eventPublisher.publish(EventTopics.AUDIT, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "EXIT", "inventoryId", inventory.getId(), "quantity", inventory.getQuantity()));

        return apiResponseGeneric("Inventory exit movement registered successfully");

    }

    @Transactional
    public ApiResponseGeneric consume (Long inventoryId,double quantity,String reason){
        Inventory inventory=inventoryRepository.findByIdAndActiveTrue(inventoryId).orElseThrow(() -> new NotFoundException("Inventory not found"));

        if(quantity>inventory.getQuantity()){
            throw new BusinessException("Not enough quantity");
        }
        if(quantity <= 0){
            throw new BusinessException("Quantity must be greater than zero");
        }
        if(!inventory.getType().equals(Type.RAW)){
            throw new BusinessException("Inventory type must be RAW");
        }

        inventory.setQuantity(inventory.getQuantity()-quantity);
        inventoryRepository.save(inventory);
        inventoryMovementService.inventoryMovement(inventory.getId(), quantity, MovementType.EXIT, "Consumption");
        eventPublisher.publish(EventTopics.INVENTORY, "INVENTORY", inventory.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("inventoryName", inventory.getName(), "action", "CONSUME", "quantity", inventory.getQuantity(),"status",inventory.getStatus().name()));
        return apiResponseGeneric("Inventory consumed successfully");

    }

    @Transactional
    public void addStockProduction(Long inventoryId,double quantity,String reason){
        Inventory inventory=inventoryRepository.findByIdAndActiveTrue(inventoryId).orElseThrow(() -> new NotFoundException("Inventory not found"));

        if(!inventory.getType().equals(Type.FINISHED)){
            throw new BusinessException("Inventory type must be FINISHED");
        }
        if(quantity <= 0){
            throw new BusinessException("Quantity must be greater than zero");
        }
        inventory.setQuantity(inventory.getQuantity()+quantity);
        inventoryRepository.save(inventory);
        inventoryMovementService.inventoryMovement(inventory.getId(), quantity, MovementType.ENTRY, reason);
    }

    @Transactional public ApiResponseGeneric moveToWaste(Long inventoryId, double quantity,String reason) {
            Inventory source = inventoryRepository.findByIdAndActiveTrue(inventoryId).orElseThrow(() -> new NotFoundException("Inventory not found"));

            if(quantity<=0){
                throw new BusinessException("Quantity must be greater than zero");
            }
            if(source.getQuantity()<quantity){
                throw new BusinessException("Not enough quantity");
            }
            source.setQuantity(source.getQuantity()-quantity);
            source.updateStatus();
            inventoryRepository.save(source);

            Inventory waste = getOrCreateWaste();
            waste.setQuantity(waste.getQuantity()+quantity);
            inventoryRepository.save(waste);
            inventoryMovementService.inventoryMovement(waste.getId(), quantity, MovementType.WASTE, reason);

            eventPublisher.publish(EventTopics.AUDIT, "INVENTORY", waste.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "MOVE_TO_WASTE", "inventoryId", waste.getId(), "quantity", quantity));
            return apiResponseGeneric("Inventory moved to waste successfully");
    }

    @Transactional
    public ApiResponseGeneric registerProductionWaste(Long productId, double quantity, String reason) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }

        String productName = inventoryRepository.findById(productId)
                .map(Inventory::getName)
                .orElse("Produto desconhecido");

        Inventory waste = getOrCreateWaste();
        waste.setQuantity(waste.getQuantity() + quantity);
        inventoryRepository.save(waste);

        String movementReason = reason != null && !reason.isBlank()
                ? reason
                : "Desperdício de produção";
        if (!movementReason.contains(productName)) {
            movementReason = productName + " — " + movementReason;
        }

        inventoryMovementService.inventoryMovement(waste.getId(), quantity, MovementType.WASTE, movementReason);

        eventPublisher.publish(EventTopics.AUDIT, "INVENTORY", waste.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(),
                Map.of("action", "PRODUCTION_WASTE", "productId", productId, "productName", productName, "quantity", quantity));
        return apiResponseGeneric("Production waste registered successfully");
    }

    private Inventory getOrCreateWaste() {
        Inventory waste = inventoryRepository.findByType(Type.WASTE)
                .orElseGet(() -> {
                    Inventory newWaste = new Inventory();
                    newWaste.setName("Desperdício de Produção");
                    newWaste.setCategory("DESPERDÍCIO");
                    newWaste.setLocation("SETOR DESCARTE");
                    newWaste.setSupplier("KIZUNA — Rejeição QC");
                    newWaste.setType(Type.WASTE);
                    newWaste.setQuantity(0.0);
                    newWaste.setMinStock(0.0);
                    newWaste.setActive(true);
                    newWaste.setStatus(Status.GOOD);
                    return inventoryRepository.save(newWaste);
                });
        normalizeWasteInventory(waste);
        return inventoryRepository.save(waste);
    }

    private void normalizeWasteInventory(Inventory waste) {
        if (waste.getName() == null || waste.getName().isBlank()) {
            waste.setName("Desperdício de Produção");
        }
        if (waste.getCategory() == null || waste.getCategory().isBlank()) {
            waste.setCategory("DESPERDÍCIO");
        }
        if (waste.getLocation() == null || waste.getLocation().isBlank()) {
            waste.setLocation("SETOR DESCARTE");
        }
        if (waste.getSupplier() == null || waste.getSupplier().isBlank()) {
            waste.setSupplier("KIZUNA — Rejeição QC");
        }
        if (waste.getQuantity() == null) {
            waste.setQuantity(0.0);
        }
        if (waste.getMinStock() == null) {
            waste.setMinStock(0.0);
        }
        if (waste.getStatus() == null) {
            waste.setStatus(Status.GOOD);
        }
        waste.setActive(true);
    }

    private InventoryResponseDto toResponseDto(Inventory inventory){
        return InventoryResponseDto.builder()
                .id(inventory.getId())
                .name(inventory.getName())
                .active(inventory.getActive())
                .status(inventory.getStatus())
                .supplier(inventory.getSupplier())
                .category(inventory.getCategory())
                .location(inventory.getLocation())
                .quantity(inventory.getQuantity())
                .minStock(inventory.getMinStock())
                .type(inventory.getType())
                .build();
    }
    private ApiResponseGeneric apiResponseGeneric(String message){
        return new ApiResponseGeneric(message, LocalDateTime.now());
    }


    }



