package com.kizuna.data_service.integration.read.dto;

import java.time.Instant;

public record IntegrationInfoDto(
        String apiVersion,
        String service,
        boolean readOnly,
        Instant serverTime
) {
}
