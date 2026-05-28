package Kizuna_core_service.qualityInspection.dto;

import java.time.LocalDateTime;

public record QualityInspectionEventMessage(Long orderId, String productName, String type, String result, LocalDateTime timestamp) {
}
