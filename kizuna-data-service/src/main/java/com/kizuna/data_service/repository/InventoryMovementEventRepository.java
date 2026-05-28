package com.kizuna.data_service.repository;

import com.kizuna.data_service.domain.InventoryMovementEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryMovementEventRepository extends MongoRepository<InventoryMovementEvent, String> {
    Long countByMovementType(String movementType);

}
