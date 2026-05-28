package com.kizuna.data_service.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "inventory_events")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    @Id
    private String id;

    private Long inventoryId;
    private String inventoryName;
    private String status;
    private boolean active=true;

    private String type;

    private Number quantity;

    private LocalDateTime timestamp;
}