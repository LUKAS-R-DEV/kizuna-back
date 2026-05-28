package com.kizuna.data_service.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kizuna.integration")
public record IntegrationProperties(
        boolean enabled,
        String apiKeyHeader,
        String bootstrapKey,
        int maxPageSize
) {
    public IntegrationProperties {
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            apiKeyHeader = "X-API-Key";
        }
        if (maxPageSize <= 0) {
            maxPageSize = 200;
        }
    }
}
