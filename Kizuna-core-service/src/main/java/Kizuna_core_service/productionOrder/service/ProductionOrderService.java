package Kizuna_core_service.productionOrder.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Type;
import Kizuna_core_service.inventory.service.InventoryService;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.dto.ProductionOrderRequestDto;
import Kizuna_core_service.productionOrder.dto.ProductionOrderResponseDto;
import Kizuna_core_service.shared.dto.ApiResponseGeneric;
import Kizuna_core_service.shared.integration.KeycloakValidation;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import Kizuna_core_service.recipe.domain.Recipe;
import Kizuna_core_service.recipe.domain.RecipeItem;
import Kizuna_core_service.recipe.repository.RecipeRepository;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import Kizuna_core_service.shared.integration.UserResponseDto;
import Kizuna_core_service.shared.messaging.EventTopics;
import Kizuna_core_service.shared.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProductionOrderService {
    private final ProductionOrderRepository productionOrderRepository;
    private final RecipeRepository recipeRepository;
    private final EventPublisher eventPublisher;
    private final InventoryService inventoryService;
    private final OperatorValidateService operatorValidateService;
    private final ProductionOrderCalculate productionOrderCalculate;
    private final ProductionRealtimePublisher productionRealtimePublisher;



    public List<ProductionOrderResponseDto> findAll() {
        Boolean isAdmin = SecurityUtils.getRoles().contains("ADMIN");
        Boolean isPlanner = SecurityUtils.getRoles().contains("PLANNER");

        if(isAdmin || isPlanner){
            return productionOrderRepository.findAll().stream().map(this::productionOrderResponseDto).toList();
        }
        String userId=SecurityUtils.getUserId();
        return productionOrderRepository.findByOperatorId(userId).stream().map(this::productionOrderResponseDto).toList();
    }

    public ProductionOrderResponseDto findById(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));
        return productionOrderResponseDto(productionOrder);
    }

    public List<ProductionOrderResponseDto> findByStatus(ProductionOrderStatus status) {
        return productionOrderRepository.findByStatus(status).stream().map(this::productionOrderResponseDto).toList();
    }

    @Transactional
    public ProductionOrderResponseDto create(ProductionOrderRequestDto requestDto) {

        Recipe recipe = recipeRepository.findByIdAndActiveTrue(requestDto.recipeId()).orElseThrow(() -> new NotFoundException("Recipe not found"));
        ProductionOrder productionOrder = new ProductionOrder();
        productionOrder.setQuantityToProduce(requestDto.quantityToProduce());
        productionOrder.setStatus(ProductionOrderStatus.PLANNED);
        productionOrder.setCreatedBy(SecurityUtils.getUsername());
        productionOrder.setRecipe(recipe);
        productionOrder.setPriority(requestDto.priority());
        productionOrder.setDeadline(requestDto.deadline());
        if (productionOrder.getDeadline() != null && productionOrder.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Deadline must be after current date");
        }
        UserResponseDto operator = operatorValidateService.validateOperator(requestDto.operatorId());
        productionOrder.setOperatorId(operator.keycloakId());
        productionOrder.setOperatorName(operator.username());
        Long estimatedTime = 0L;
        if (recipe.getEstimatedProductionTime() != null) {
            estimatedTime = productionOrder.getQuantityToProduce() * recipe.getEstimatedProductionTime();
        }
        productionOrder.setEstimatedTotalTime(estimatedTime);
        productionOrderRepository.save(productionOrder);
        reorderQueue();

        eventPublisher.publish(EventTopics.PRODUCTION_CREATED, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("recipeName", productionOrder.getRecipe().getName(), "status", "PLANNED","quantity",productionOrder.getQuantityToProduce()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "CREATE", "recipeName", recipe.getName()));

        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.created", "userId", productionOrder.getOperatorId(), "title", "Nova Ordem de Produção", "message", "Você foi designado para produzir: " + recipe.getName()));

        return productionOrderResponseDto(productionOrder);
    }

    @Transactional
    public ProductionOrderResponseDto start(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));
        if (!(productionOrder.getStatus().equals(ProductionOrderStatus.PLANNED) || productionOrder.getStatus().equals(ProductionOrderStatus.PAUSED))) {
            throw new BusinessException("Production order is not planned or paused");
        }
        Recipe recipe = productionOrder.getRecipe();

        if (recipe.getItems().isEmpty()) {
            throw new BusinessException("Recipe has no items");
        }

        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        List<InventoryMovement> movements = new ArrayList<>();
        for (RecipeItem item : recipe.getItems()) {
            Inventory inventory = item.getInventory();
            Double consumption = item.getQuantity() * productionOrder.getQuantityToProduce();
            if (inventory.getQuantity() < consumption) {
                throw new BusinessException("Not enough stock for item " + inventory.getName());
            }
            if(!inventory.getType().equals(Type.RAW)){
                throw new BusinessException("Inventory is not raw");
            }
        }
        for (RecipeItem item : recipe.getItems()) {
            Inventory inventory = item.getInventory();
            Double consumption = item.getQuantity() * productionOrder.getQuantityToProduce();
            String reason = "Production order ID: " + productionOrder.getId();
            inventoryService.consume(inventory.getId(), consumption,reason);
            inventoriesToUpdate.add(inventory);
        }
        productionOrder.setStatus(ProductionOrderStatus.IN_PROGRESS);
        productionOrder.setLastStartTime(LocalDateTime.now());

        if (productionOrder.getStartTime() == null) {
            productionOrder.setStartTime(LocalDateTime.now());
        }
        productionOrderRepository.save(productionOrder);
        reorderQueue();
        eventPublisher.publish(EventTopics.PRODUCTION_STARTED, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("recipeName", productionOrder.getRecipe().getName(), "status", "START", "quantity", productionOrder.getQuantityToProduce()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "START", "recipeName", recipe.getName()));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.started",  "title", "Ordem de Produção Iniciada", "message", "A ordem de produção foi iniciada: " + productionOrder.getRecipe().getName(),"targetRole", "PLANNER"));
        productionRealtimePublisher.publishOrder(productionOrder);
        return productionOrderResponseDto(productionOrder);

    }
    @Transactional
    public ApiResponseGeneric pause(Long id){
        ProductionOrder productionOrder = productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));

        if(!productionOrder.getStatus().equals(ProductionOrderStatus.IN_PROGRESS)){
            throw new BusinessException("Production order must be IN_PROGRESS to pause");
        }

        if(!SecurityUtils.getUserId().equals(productionOrder.getOperatorId())){
            throw new BusinessException("You are not the operator of this production order");
        }

        if (productionOrder.getLastStartTime() != null) {
            long elapsed = Duration.between(productionOrder.getLastStartTime(), LocalDateTime.now()).toMinutes();
            long worked = productionOrder.getWorkedMinutes() != null ? productionOrder.getWorkedMinutes() : 0;
            productionOrder.setWorkedMinutes(worked + elapsed);
        }
        productionOrder.setLastStartTime(null);
        productionOrder.setStatus(ProductionOrderStatus.PAUSED);
        productionOrderRepository.save(productionOrder);
        reorderQueue();
        eventPublisher.publish(EventTopics.PRODUCTION_PAUSED, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("recipeName", productionOrder.getRecipe().getName(), "status", "PAUSED", "quantity", productionOrder.getQuantityToProduce()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "PAUSE", "recipeName", productionOrder.getRecipe().getName()));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.paused",  "title", "Ordem de Produção Pausada", "message", "A ordem de produção foi pausada: " + productionOrder.getRecipe().getName(),"targetRole", "PLANNER"));
        productionRealtimePublisher.publishOrder(productionOrder);
        return new ApiResponseGeneric("Production order paused successfully",LocalDateTime.now());

    }

    @Transactional
    public ApiResponseGeneric resume(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Production order not found"));

        if (!productionOrder.getStatus().equals(ProductionOrderStatus.PAUSED)) {
            throw new BusinessException("Production order must be PAUSED to resume");
        }

        if (!SecurityUtils.getUserId().equals(productionOrder.getOperatorId())) {
            throw new BusinessException("You are not the operator of this production order");
        }

        productionOrder.setStatus(ProductionOrderStatus.IN_PROGRESS);
        productionOrder.setLastStartTime(LocalDateTime.now());
        productionOrderRepository.save(productionOrder);
        reorderQueue();
        eventPublisher.publish(EventTopics.PRODUCTION_STARTED, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("recipeName", productionOrder.getRecipe().getName(), "status", "RESUME", "quantity", productionOrder.getQuantityToProduce()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "RESUME", "recipeName", productionOrder.getRecipe().getName()));
        productionRealtimePublisher.publishOrder(productionOrder);
        return apiResponseGeneric("Production order resumed successfully");
    }

    @Transactional
    public ApiResponseGeneric  reworkProductionOrder(Long id){
        ProductionOrder productionOrder = productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));

        if(!productionOrder.getStatus().equals(ProductionOrderStatus.REWORK)){
            throw new BusinessException("Production order must be REWORK to rework");
        }

        if(productionOrder.getReworkCount() != null && productionOrder.getReworkCount() >= 1){
            throw new BusinessException("Production order already reworked");
        }
        productionOrder.setWorkedMinutes(0L);
        productionOrder.setStatus(ProductionOrderStatus.IN_PROGRESS);
        productionOrder.setStartTime(LocalDateTime.now());
        productionOrder.setLastStartTime(LocalDateTime.now());
        productionOrder.setEndTime(null);
        Integer reworkCount = productionOrder.getReworkCount() == null ? 1 : productionOrder.getReworkCount() + 1;
        productionOrder.setReworkCount(reworkCount);

        productionOrderRepository.save(productionOrder);
        reorderQueue();
        eventPublisher.publish(EventTopics.PRODUCTION_REWORK, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("recipeName", productionOrder.getRecipe().getName(), "status", "REWORK", "quantity", productionOrder.getQuantityToProduce()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "REWORK", "recipeName", productionOrder.getRecipe().getName()));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.rework", "title", "Ordem de Produção Reexecutada", "message", "A ordem de produção foi reexecutada: " + productionOrder.getRecipe().getName(),"targetRole", "PLANNER"));
        productionRealtimePublisher.publishOrder(productionOrder);

        return apiResponseGeneric("Production order rework started");
    }

    @Transactional
    public ProductionOrderResponseDto finish(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));
        if (!productionOrder.getStatus().equals(ProductionOrderStatus.IN_PROGRESS)) {
            throw new BusinessException("Production order is not in progress");
        }
        if (productionOrder.getLastStartTime() != null) {
            long elapsed = Duration.between(productionOrder.getLastStartTime(), LocalDateTime.now()).toMinutes();
            long worked = productionOrder.getWorkedMinutes() != null ? productionOrder.getWorkedMinutes() : 0;
            productionOrder.setWorkedMinutes(worked + elapsed);
            productionOrder.setLastStartTime(null);
        }
        productionOrder.setStatus(ProductionOrderStatus.WAITING_INSPECTION);
        productionOrder.setEndTime(LocalDateTime.now());
        productionOrderRepository.save(productionOrder);
        productionRealtimePublisher.publishOrder(productionOrder);
        eventPublisher.publish(EventTopics.PRODUCTION_FINISHED, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("recipeName", productionOrder.getRecipe().getName(), "status", "FINISH", "quantity", productionOrder.getQuantityToProduce()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "FINISH", "recipeName", productionOrder.getRecipe().getName()));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.finished", "title", "Ordem de Produção Finalizada", "message", "A ordem de produção foi finalizada: " + productionOrder.getRecipe().getName(),"targetRole", "PLANNER"));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getCreatedBy(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.finished","title", "Ordem de Produção Aguardando inspeção", "message", "A ordem de produção foi finalizada: " + productionOrder.getRecipe().getName(),"targetRole", "INSPECTOR"));
        return productionOrderResponseDto(productionOrder);
    }

    @Transactional
    public ProductionOrderResponseDto cancel(Long id) {
        ProductionOrder productionOrder = productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));
        if (productionOrder.getStatus().equals(ProductionOrderStatus.WAITING_INSPECTION) || productionOrder.getStatus().equals(Kizuna_core_service.productionOrder.domain.ProductionOrderStatus.FINISHED_BY_TIME)) {
            throw new BusinessException("Production order is already completed");
        }
        if(productionOrder.getStatus() != (ProductionOrderStatus.PLANNED)){
            throw new BusinessException("Production order is not planned");
        }
        productionOrder.setStatus(ProductionOrderStatus.CANCELLED);
        productionOrder.setEndTime(LocalDateTime.now());
        productionOrderRepository.save(productionOrder);
        reorderQueue();
        eventPublisher.publish(EventTopics.PRODUCTION_ORDER, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("recipeName", productionOrder.getRecipe().getName(), "status", "CANCELLED", "quantity", productionOrder.getQuantityToProduce()));
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", productionOrder.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "CANCELLED", "recipeName", productionOrder.getRecipe().getName()));
        eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", productionOrder.getCreatedBy(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.cancelled", "userId", productionOrder.getOperatorId(), "title", "Ordem de Produção Cancelada", "message", "A ordem de produção foi cancelada: " + productionOrder.getRecipe().getName()));
        return productionOrderResponseDto(productionOrder);
    }
    public void moveOrder(Long id, int newPosition) {

        List<ProductionOrder> queue = productionOrderRepository
                .findByStatus(ProductionOrderStatus.PLANNED);

        queue.sort(Comparator.comparing(ProductionOrder::getQueuePosition));

        ProductionOrder order = queue.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order not found"));

        queue.remove(order);

        int targetIndex=Math.max(0, newPosition -1);

        if(targetIndex > queue.size()){
            targetIndex = queue.size();
        }

        queue.add(targetIndex, order);

        for (int i = 0; i < queue.size(); i++) {
            queue.get(i).setQueuePosition(i + 1);
        }

        productionOrderRepository.saveAll(queue);
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", id.toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "REORDER", "recipeName", order.getRecipe().getName()));
    }

    @Transactional
    public ApiResponseGeneric autoFinishByTime() {
        List<ProductionOrder> orders = productionOrderRepository
                .findByStatus(ProductionOrderStatus.IN_PROGRESS);

        for (ProductionOrder order : orders) {
            if (order.getEstimatedTotalTime() == null) continue;
            if (productionOrderCalculate.getWorkedMinutes(order) >= order.getEstimatedTotalTime()) {
                order.setStatus(ProductionOrderStatus.WAITING_INSPECTION);
                order.setEndTime(LocalDateTime.now());
                productionOrderRepository.save(order);
                productionRealtimePublisher.publishOrder(order);
                eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", order.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "FINISHED_BY_TIME", "recipeName", order.getRecipe().getName()));
                eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", order.getCreatedBy(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.finished_by_time", "userId", order.getCreatedBy(), "title", "Ordem de Produção Finalizada", "message", "A ordem de produção foi finalizada: " + order.getRecipe().getName()));
                eventPublisher.publish(EventTopics.NOTIFICATION, "NOTIFICATION", order.getOperatorId(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("eventType", "event.production.finished_by_time", "title", "Ordem de Produção Aguardando inspeção", "message", "A ordem de produção foi finalizada: " + order.getRecipe().getName(),"targetRole", "INSPECTOR"));
            }
        }
        return new ApiResponseGeneric("Production orders finished by time",LocalDateTime.now());
    }

    @Transactional
    public void reorderQueue(){
        List<ProductionOrder> orders=productionOrderRepository.findByStatus(ProductionOrderStatus.PLANNED);

        orders.sort(Comparator
                .comparing(ProductionOrder::getPriority, Comparator.nullsLast(Integer::compareTo)).reversed()
                .thenComparing(ProductionOrder::getDeadline, Comparator.nullsLast(LocalDateTime::compareTo)));

        for(int i=0;i<orders.size();i++){
            orders.get(i).setQueuePosition(i+1);
        }
        productionOrderRepository.saveAll(orders);
        eventPublisher.publish(EventTopics.AUDIT, "PRODUCTION_ORDER", orders.get(0).getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "REORDER", "queueSize", orders.size()));

    }

    private ProductionOrderResponseDto productionOrderResponseDto(ProductionOrder productionOrder) {
        return ProductionOrderResponseDto.builder().id(productionOrder.getId()).recipeName(productionOrder.getRecipe().getName())
                .quantityToProduce(productionOrder.getQuantityToProduce())
                .startTime(productionOrder.getStartTime())
                .createdBy(SecurityUtils.getUsername())
                .calculatedStatus(productionOrderCalculate.calculateStatus(productionOrder)).status(productionOrder.getStatus())
                .progress(productionOrderCalculate.calculateProgress(productionOrder))
                .eta(productionOrderCalculate.calculateETA(productionOrder))
                .remainingTime(productionOrderCalculate.calculateRemainingTime(productionOrder))
                .priority(productionOrder.getPriority())
                .queuePosition(productionOrder.getQueuePosition())
                .estimatedTotalTime(productionOrder.getEstimatedTotalTime())
                .endTime(productionOrder.getEndTime())
                .deadline(productionOrder.getDeadline())
                .queuePosition(productionOrder.getQueuePosition())
                .operatorId(productionOrder.getOperatorId())
                .operatorName(productionOrder.getOperatorName())
                .build();

    }

    private ApiResponseGeneric apiResponseGeneric(String message){
        return new ApiResponseGeneric(message, LocalDateTime.now());
    }

}
