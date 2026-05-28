package com.kizuna.data_service.integration.apiKey.dto;
import jakarta.validation.constraints.NotBlank;


public record ApiKeyRequestDto(
        @NotBlank
        String name) {
}
