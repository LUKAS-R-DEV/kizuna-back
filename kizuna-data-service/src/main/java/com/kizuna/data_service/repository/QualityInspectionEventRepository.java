package com.kizuna.data_service.repository;

import com.kizuna.data_service.domain.QualityInspectionEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QualityInspectionEventRepository extends MongoRepository<QualityInspectionEvent, String> {
    long countByResult(String result);

    List<QualityInspectionEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
