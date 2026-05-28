package Kizuna_core_service.qualityInspection.dto;

import Kizuna_core_service.qualityInspection.domain.QualityInspectionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QualityInspectionRequestDto(
        @NotNull(message = "Production order id is required")
        Long productionOrderId,
        @NotNull(message = "Status is required")
        QualityInspectionStatus
        status,
        @NotBlank(message = "Notes is required")
        String notes) {

}
