package Kizuna_core_service.inventory.domain;

import Kizuna_core_service.shared.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String location;
    private Double quantity;
    private Double minStock;
    private String supplier;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Type type;
    private Boolean active=true;

    @PrePersist
    @PreUpdate
    public void updateStatus() {
        if (this.type == Type.WASTE) {
            this.status = Status.GOOD;
            return;
        }
        double qty = this.quantity != null ? this.quantity : 0.0;
        double min = this.minStock != null ? this.minStock : 0.0;
        this.status = qty <= min ? Status.CRITICAL : Status.GOOD;
    }

}