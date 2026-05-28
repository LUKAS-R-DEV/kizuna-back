package Kizuna_core_service.productionOrder.domain;

public enum ProductionOrderStatus {
    PLANNED,
    IN_PROGRESS,
    WAITING_INSPECTION,
    APPROVED,
    PAUSED,
    REWORK,
    REJECTED,
    CANCELLED,
    FINISHED_BY_TIME
}
