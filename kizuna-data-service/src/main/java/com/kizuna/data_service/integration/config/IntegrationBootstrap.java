package com.kizuna.data_service.integration.config;

import com.kizuna.data_service.integration.apiKey.repository.ApiKeyRepository;
import com.kizuna.data_service.integration.apiKey.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationBootstrap implements ApplicationRunner {

    private final IntegrationProperties properties;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyService apiKeyService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        String bootstrapKey = properties.bootstrapKey();
        if (bootstrapKey == null || bootstrapKey.isBlank()) {
            return;
        }
        if (apiKeyRepository.existsByKey(bootstrapKey.trim())) {
            return;
        }
        apiKeyService.ensureKey("bootstrap", bootstrapKey.trim());
        log.info("Integration API key bootstrap registered (name=bootstrap)");
    }
}
