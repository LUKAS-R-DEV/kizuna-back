package com.kizuna.data_service.repository;

import com.kizuna.data_service.domain.ProductionEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionEventRepository extends MongoRepository<ProductionEvent, String> {
    Long countByType(String type);
    Optional<ProductionEvent> findByOrderId(Long orderId);
    List<ProductionEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
