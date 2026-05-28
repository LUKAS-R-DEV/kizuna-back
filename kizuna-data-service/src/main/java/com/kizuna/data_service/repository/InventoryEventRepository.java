package com.kizuna.data_service.repository;

import com.kizuna.data_service.domain.InventoryEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryEventRepository extends MongoRepository<InventoryEvent, String> {
    long countByQuantityLessThan(int quantity);

    Optional<InventoryEvent> findByInventoryId(Long inventoryId);

    List<InventoryEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
