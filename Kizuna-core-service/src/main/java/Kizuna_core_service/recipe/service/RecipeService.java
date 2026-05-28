package Kizuna_core_service.recipe.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Type;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.recipe.domain.Recipe;
import Kizuna_core_service.recipe.domain.RecipeItem;
import Kizuna_core_service.recipe.dto.*;
import Kizuna_core_service.recipe.repository.RecipeRepository;
import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.messaging.EventTopics;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import Kizuna_core_service.shared.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
@AllArgsConstructor
@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final InventoryRepository inventoryRepository;
    private final EventPublisher eventPublisher;

    public Set<RecipeResponseDto> findAll(){
        return recipeRepository.findByActiveTrue().stream().map(this::recipeResponseDto).collect(Collectors.toSet());
    }
    public RecipeResponseDto findById(Long id){
        Recipe recipe = recipeRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new NotFoundException("Recipe not found"));
        return recipeResponseDto(recipe);
    }

    @Transactional
    public RecipeResponseDto create(RecipeRequestDto requestDto){
        Inventory product=inventoryRepository.findByIdAndActiveTrue(requestDto.productId()).orElseThrow(() -> new NotFoundException("Product not found"));
        if(!product.getType().equals(Type.FINISHED)){
            throw new BusinessException("Product must be finished");
        }

        if(recipeRepository.existsByProductAndActiveTrue(product)){
            throw new BusinessException("Recipe already exists");
        }

        Recipe recipe=new Recipe();
        String recipeName = requestDto.name() != null && !requestDto.name().isBlank()
                ? requestDto.name().trim()
                : product.getName();
        recipe.setName(recipeName);
        recipe.setDescription(requestDto.description());
        recipe.setProduct(product);
        recipe.setEstimatedProductionTime(requestDto.estimatedProductionTime());
        recipe.setActive(true);


        if(requestDto.items().isEmpty()){
            throw new BusinessException("Recipe must have at least one ingredient");
        }
        for(RecipeItemRequestDto itemRequestDto: requestDto.items()){
            Inventory inventoryItem=inventoryRepository.findByIdAndActiveTrue(itemRequestDto.inventoryId()).orElseThrow(() -> new NotFoundException("Inventory not found"));
            if(!inventoryItem.getType().equals(Type.RAW)){
                throw new BusinessException("Inventory must be raw");
            }
            RecipeItem recipeItem=new RecipeItem();
            recipeItem.setQuantity(itemRequestDto.quantity());
            recipeItem.setInventory(inventoryItem);
            recipe.addItem(recipeItem);
        }
        recipeRepository.save(recipe);
        eventPublisher.publish(EventTopics.RECIPE_CREATED, "RECIPE", recipe.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "CREATE", "productName", product.getName()));
        eventPublisher.publish(EventTopics.AUDIT, "RECIPE", recipe.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "CREATE", "productName", product.getName()));
        return recipeResponseDto(recipe);

    }

    @Transactional
    public RecipeResponseDto update(Long id, RecipeUpdateDto updateDto){
        Inventory product=inventoryRepository.findByIdAndActiveTrue(updateDto.productId()).orElseThrow(() -> new NotFoundException("Product not found"));
        Recipe recipe=recipeRepository.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found"));

        recipe.setName(updateDto.name());
        recipe.setDescription(updateDto.description());
        recipe.setProduct(product);
        recipe.setEstimatedProductionTime(updateDto.estimatedProductionTime());

        recipe.getItems().clear();

        for(RecipeItemRequestDto itemDto: updateDto.items()){
            Inventory inventory=inventoryRepository.findByIdAndActiveTrue(itemDto.inventoryId()).orElseThrow(() -> new NotFoundException("Inventory not found"));
            RecipeItem recipeItem=new RecipeItem();
            recipeItem.setQuantity(itemDto.quantity());
            recipeItem.setInventory(inventory);
            recipe.addItem(recipeItem);
        }
        recipeRepository.save(recipe);
        eventPublisher.publish(EventTopics.RECIPE_UPDATED, "RECIPE", recipe.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "UPDATE", "productName", product.getName()));
        eventPublisher.publish(EventTopics.AUDIT, "RECIPE", recipe.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "UPDATE", "productName", product.getName()));
        return recipeResponseDto(recipe);

    }
@Transactional
    public RecipeResponseDto disable(Long id){
        Recipe recipe=recipeRepository.findByIdAndActiveTrue(id).orElseThrow(()->new NotFoundException("Recipe not found"));
        recipe.setActive(false);
        recipeRepository.save(recipe);
        eventPublisher.publish(EventTopics.RECIPE_DISABLED, "RECIPE", recipe.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "DISABLE", "productName", recipe.getProduct().getName()));
        eventPublisher.publish(EventTopics.AUDIT, "RECIPE", recipe.getId().toString(), SecurityUtils.getUserId(), SecurityUtils.getUsername(), Map.of("action", "DISABLE", "productName", recipe.getProduct().getName()));
        return recipeResponseDto(recipe);
    }

    private RecipeResponseDto recipeResponseDto(Recipe recipe) {
        return new RecipeResponseDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getProduct().getId(),
                recipe.getProduct().getName(),
                recipe.getDescription(),
                recipe.getItems().stream().map(this::recipeItemResponseDto).collect(Collectors.toSet()),
                recipe.getEstimatedProductionTime()
        );
    }
    private final RecipeItemResponseDto recipeItemResponseDto(RecipeItem recipeItem){
        return new RecipeItemResponseDto(recipeItem.getInventory().getName(),recipeItem.getInventory().getId(),recipeItem.getQuantity());
    }

}
