package com.kizuna.data_service.integration.apiKey.service;

import com.kizuna.data_service.integration.apiKey.domain.ApiKey;
import com.kizuna.data_service.integration.apiKey.dto.ApiKeyRequestDto;
import com.kizuna.data_service.integration.apiKey.dto.ApiKeyResponseDto;
import com.kizuna.data_service.integration.apiKey.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository repository;

    public ApiKeyResponseDto create(ApiKeyRequestDto request) {
        String key = ApiKeyGeneratorService.generate();
        ApiKey apiKey = ApiKey.builder()
                .name(request.name().trim())
                .key(key)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
        ApiKey saved = repository.save(apiKey);
        return toResponse(saved, key);
    }

    public void ensureKey(String name, String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("API key value is required");
        }
        String normalized = key.trim();
        if (repository.existsByKey(normalized)) {
            return;
        }
        repository.save(ApiKey.builder()
                .name(name.trim())
                .key(normalized)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build());
    }

    public List<ApiKeyResponseDto> findAllMasked() {
        return repository.findByActiveTrue().stream()
                .map(key -> toResponse(key, null))
                .toList();
    }

    public void disable(String id) {
        ApiKey apiKey = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found"));
        apiKey.setActive(false);
        repository.save(apiKey);
    }

    public boolean isValid(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return repository.findByKeyAndActiveTrue(key.trim()).isPresent();
    }

    private static ApiKeyResponseDto toResponse(ApiKey apiKey, String plainKey) {
        String keyValue = plainKey != null ? plainKey : maskKey(apiKey.getKey());
        return new ApiKeyResponseDto(
                apiKey.getId(),
                apiKey.getName(),
                keyValue,
                apiKey.getCreatedAt(),
                apiKey.isActive()
        );
    }

    private static String maskKey(String key) {
        if (key == null || key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
    }
}
