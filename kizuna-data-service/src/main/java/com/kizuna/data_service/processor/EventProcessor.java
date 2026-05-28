package com.kizuna.data_service.processor;

import com.kizuna.data_service.domain.*;
import com.kizuna.data_service.dto.GenericEventDto;
import com.kizuna.data_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventProcessor {
  private final InventoryEventRepository inventoryEventRepository;
  private final ProductionEventRepository productionEventRepository;
  private final RecipeEventRepository recipeEventRepository;
  private final QualityInspectionEventRepository qualityInspectionEventRepository;
  private final InventoryMovementEventRepository inventoryMovementEventRepository;





  public void process (GenericEventDto eventDto){
      if (eventDto.entityId() == null || eventDto.entityId().isBlank()) {
          System.err.println("[DATA-SERVICE] Invalid message: entityId is null or blank");
          return;
      }


      switch (eventDto.entity()){
          case "PRODUCTION_ORDER" -> handleProduction(eventDto);
          case "INVENTORY" -> handleInventory(eventDto);
          case "INVENTORY_MOVEMENT" -> handleInventoryMovement(eventDto);
          case "QUALITY_INSPECTION" -> handleInspection(eventDto);
          case "RECIPE" -> handleRecipe(eventDto);

          default -> System.out.println("[DATA-SERVICE] Unknown event entity: " + eventDto.entity());

      }
  }






  private void handleProduction(GenericEventDto eventDto){
      ProductionEvent existing = productionEventRepository.findByOrderId(Long.valueOf(eventDto.entityId()))
              .orElse(new ProductionEvent());

      ProductionEvent productionEvent = existing.toBuilder()
              .orderId(Long.valueOf(eventDto.entityId()))
              .type(eventDto.entity())
              .timestamp(eventDto.timestamp())
              .recipeName(eventDto.data().get("recipeName") != null ?
                      (String) eventDto.data().get("recipeName") : existing.getRecipeName())
              .quantity(eventDto.data().get("quantity") != null ?
                      (Number) eventDto.data().get("quantity") : existing.getQuantity())
              .status(eventDto.data().get("status") != null ?
                      (String) eventDto.data().get("status") : existing.getStatus())
              .build();
      productionEventRepository.save(productionEvent);
      System.out.println("Evento production salvo");
  }

  private void handleInventory (GenericEventDto eventDto){
      Long inventoryId = Long.valueOf(eventDto.entityId());
      InventoryEvent existing = inventoryEventRepository.findByInventoryId(inventoryId)
              .orElse(new InventoryEvent());

      InventoryEvent inventoryEvent = existing.toBuilder()
              .inventoryId(inventoryId)
              .inventoryName(eventDto.data().get("inventoryName") != null ?
                      (String) eventDto.data().get("inventoryName") : existing.getInventoryName())
              .timestamp(eventDto.timestamp())
              .type(eventDto.data().get("status") != null ?
                      (String) eventDto.data().get("status") : existing.getType())
              .quantity(eventDto.data().get("quantity") != null ?
                      (Number) eventDto.data().get("quantity") : existing.getQuantity())
              .active(eventDto.data().get("active") != null ?
                      (Boolean) eventDto.data().get("active") : existing.isActive())
              .build();

      inventoryEventRepository.save(inventoryEvent);
      System.out.println("Evento inventory salvo");

  }

  private void handleInventoryMovement(GenericEventDto eventDto){
      InventoryMovementEvent inventoryMovementEvent = InventoryMovementEvent.builder()
              .inventoryId(Long.valueOf(eventDto.entityId()))
              .movementType(eventDto.eventType())
              .timestamp(eventDto.timestamp())
              .inventoryName((String) eventDto.data().get("inventoryName"))
              .quantity((Number) eventDto.data().get("quantity"))
              .build();
      inventoryMovementEventRepository.save(inventoryMovementEvent);
      System.out.println("Evento inventory movement salvo");
  }

  private void handleInspection(GenericEventDto eventDto){
      QualityInspectionEvent qualityInspectionEvent = QualityInspectionEvent.builder()
              .orderId(Long.valueOf(eventDto.entityId()))
              .result((String) eventDto.data().get("result"))
              .timestamp(eventDto.timestamp())
              .type(eventDto.eventType())
              .productName((String) eventDto.data().get("productName"))
              .build();
      qualityInspectionEventRepository.save(qualityInspectionEvent);
      System.out.println("Evento quality inspection salvo");
  }
  private void handleRecipe (GenericEventDto eventDto){
      RecipeEvent recipeEvent= RecipeEvent.builder()
              .recipeId(Long.valueOf(eventDto.entityId()))
              .recipeName((String) eventDto.data().get("recipeName"))
              .type(eventDto.eventType())
              .timestamp(eventDto.timestamp())
              .build();
      recipeEventRepository.save(recipeEvent);
      System.out.println("Evento recipe salvo");
  }

}
