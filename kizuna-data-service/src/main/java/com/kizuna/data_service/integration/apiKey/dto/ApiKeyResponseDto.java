package com.kizuna.data_service.integration.apiKey.dto;

import java.time.LocalDateTime;

public record ApiKeyResponseDto(String id, String name, String key, LocalDateTime createdAt, boolean active) {
}
