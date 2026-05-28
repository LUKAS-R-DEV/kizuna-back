package com.kizuna.data_service.integration.apiKey.repository;

import com.kizuna.data_service.integration.apiKey.domain.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyAndActiveTrue(String key);

    boolean existsByKey(String key);
    List<ApiKey> findByActiveTrue();
}
