package Kizuna_core_service.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RecipeRequestDto(
        @NotBlank(message = "Name cannot be null or empty")
        String name,
        @NotBlank(message = "Description cannot be null or empty")
        String description,
        @NotEmpty(message = "Items cannot be empty")
        Set<RecipeItemRequestDto> items,
        @NotNull(message = "Estimated product time cannot be null")
        Long estimatedProductionTime,
        @NotNull(message = "Product ID cannot be null")
        Long productId) {

}
