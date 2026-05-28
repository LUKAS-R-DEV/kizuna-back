package com.kizuna.data_service.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "inventory_movement_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementEvent {

    @Id
    private String id;

    private Long inventoryId;
    private String inventoryName;

    private String movementType;
    private Number quantity;

    private LocalDateTime timestamp;
}