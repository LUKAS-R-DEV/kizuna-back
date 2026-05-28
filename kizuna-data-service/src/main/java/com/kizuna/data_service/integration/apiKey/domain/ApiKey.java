package com.kizuna.data_service.integration.apiKey.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "api_keys")
@Data
@Builder
public class ApiKey {
    @Id
    private String id;
    private String name;
    private String key;
    private LocalDateTime createdAt;
    private boolean active = true;
}
