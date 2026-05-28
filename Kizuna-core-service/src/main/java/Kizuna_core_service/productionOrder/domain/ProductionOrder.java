package Kizuna_core_service.productionOrder.domain;

import Kizuna_core_service.recipe.domain.Recipe;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_order")
@Data
public class ProductionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantityToProduce;
    private LocalDateTime startTime;
    private LocalDateTime lastStartTime;
    private Long workedMinutes;
    private Long estimatedTotalTime;
    private String operatorName;
    private String operatorId;
    private LocalDateTime endTime;
    private Integer reworkCount=0;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer priority;
    private LocalDateTime deadline;
    private Integer queuePosition;
    @Enumerated(EnumType.STRING)
    private ProductionOrderStatus status;
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();

    }
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
