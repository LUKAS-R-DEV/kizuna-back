package Kizuna_core_service.productionOrder.repository;

import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
List<ProductionOrder> findByStatus(ProductionOrderStatus status);
List<ProductionOrder> findByStatusIn(List<ProductionOrderStatus> statuses);
List<ProductionOrder> findByOperatorId(String operatorId);
}
