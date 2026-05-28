package Kizuna_core_service.qualityInspection.domain;

import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "quality_inspection")
@Data
public class QualityInspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;
    @Enumerated(EnumType.STRING)
    private QualityInspectionStatus status;
    private String notes;
    private String inspectedBy;
    private LocalDateTime createdAt;


    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }

}
