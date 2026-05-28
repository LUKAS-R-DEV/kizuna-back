package com.kizuna.data_service.repository;

import com.kizuna.data_service.domain.RecipeEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecipeEventRepository extends MongoRepository<RecipeEvent, String> {
}
