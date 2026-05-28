package Kizuna_core_service.recipe.repository;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.recipe.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByActiveTrue();
    boolean existsByProductAndActiveTrue(Inventory product);
    Optional<Recipe> findByIdAndActiveTrue(Long id);

}
