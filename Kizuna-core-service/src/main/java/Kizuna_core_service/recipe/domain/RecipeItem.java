package Kizuna_core_service.recipe.domain;

import Kizuna_core_service.inventory.domain.Inventory;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "recipe_item")
@Data

public class RecipeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;
    private Double quantity;
}
