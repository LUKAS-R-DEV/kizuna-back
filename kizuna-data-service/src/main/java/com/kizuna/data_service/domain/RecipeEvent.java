package com.kizuna.data_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "recipe_events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeEvent {
    @Id
    private String id;
    private Long recipeId;
    private String recipeName;
    private String type;
    private LocalDateTime timestamp;
}
