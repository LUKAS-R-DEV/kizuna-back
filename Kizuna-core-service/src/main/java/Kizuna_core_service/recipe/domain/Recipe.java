package Kizuna_core_service.recipe.domain;

import Kizuna_core_service.inventory.domain.Inventory;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "recipe")
@Data
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Boolean active=true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long estimatedProductionTime;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Inventory product;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeItem> items=new HashSet<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(id, recipe.id) && Objects.equals(name, recipe.name) && Objects.equals(description, recipe.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }


    public void addItem(RecipeItem item) {
      item.setRecipe(this);
      this.items.add(item);
    }

    public void removeItem(RecipeItem item) {
        item.setRecipe(null);
        this.items.remove(item);
       ;
    }
}
