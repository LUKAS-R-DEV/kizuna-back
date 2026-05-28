package com.kizuna.data_service.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "production_events")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductionEvent {

    @Id
    private String id;

    private Long orderId;
    private String recipeName;
    private String status;

    private String type;

    private LocalDateTime timestamp;


    private Number quantity;
    private Number priority;
}