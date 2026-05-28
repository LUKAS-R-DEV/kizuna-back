package com.kizuna.data_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "quality_inspection_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionEvent {
    @Id
    private String id;
    private Long orderId;
    private String productName;
    private String type;
    private String result;
    private LocalDateTime timestamp;
}
