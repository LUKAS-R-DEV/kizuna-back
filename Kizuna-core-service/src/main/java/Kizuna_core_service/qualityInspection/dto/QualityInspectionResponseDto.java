package Kizuna_core_service.qualityInspection.dto;

import Kizuna_core_service.qualityInspection.domain.QualityInspectionStatus;

import java.time.LocalDateTime;

public record QualityInspectionResponseDto(Long id, String productionOrderName, QualityInspectionStatus status, String notes, String inspectedBy, LocalDateTime createdAt) {

}
