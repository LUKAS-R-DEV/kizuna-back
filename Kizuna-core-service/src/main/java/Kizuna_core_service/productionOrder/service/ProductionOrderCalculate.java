package Kizuna_core_service.productionOrder.service;

import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ProductionOrderCalculate {

    public long getWorkedMinutes(ProductionOrder order) {
        long worked = order.getWorkedMinutes() != null ? order.getWorkedMinutes() : 0;

        if (order.getStatus() == ProductionOrderStatus.IN_PROGRESS) {
            LocalDateTime segmentStart = order.getLastStartTime() != null
                    ? order.getLastStartTime()
                    : order.getStartTime();
            if (segmentStart != null) {
                worked += Duration.between(segmentStart, LocalDateTime.now()).toMinutes();
            }
        }

        return worked;
    }

    public Long calculateRemainingTime(ProductionOrder order) {
        if (order.getEstimatedTotalTime() == null) {
            return null;
        }

        if (order.getStatus() == ProductionOrderStatus.REWORK) {
            return order.getEstimatedTotalTime();
        }

        long worked = getWorkedMinutes(order);
        return Math.max(0, order.getEstimatedTotalTime() - worked);
    }

    public Double calculateProgress(ProductionOrder order) {
        if (order.getEstimatedTotalTime() == null || order.getEstimatedTotalTime() == 0) {
            return 0.0;
        }

        if (order.getStatus() == ProductionOrderStatus.REWORK) {
            return 0.0;
        }

        if (order.getStatus() != ProductionOrderStatus.IN_PROGRESS) {
            return 0.0;
        }

        long worked = getWorkedMinutes(order);
        double progress = ((double) worked / order.getEstimatedTotalTime()) * 100;
        return Math.min(progress, 100.0);
    }

    public LocalDateTime calculateETA(ProductionOrder order) {
        if (order.getEstimatedTotalTime() == null) {
            return null;
        }

        if (order.getStatus() == ProductionOrderStatus.REWORK) {
            return LocalDateTime.now().plusMinutes(order.getEstimatedTotalTime());
        }

        if (order.getStatus() != ProductionOrderStatus.IN_PROGRESS) {
            return null;
        }

        Long remaining = calculateRemainingTime(order);
        if (remaining == null) {
            return null;
        }
        return LocalDateTime.now().plusMinutes(remaining);
    }

    public ProductionOrderStatus calculateStatus(ProductionOrder order) {
        if (order.getStatus() == ProductionOrderStatus.WAITING_INSPECTION
                || order.getStatus() == ProductionOrderStatus.CANCELLED
                || order.getStatus() == ProductionOrderStatus.FINISHED_BY_TIME
                || order.getStatus() == ProductionOrderStatus.PAUSED
                || order.getStatus() == ProductionOrderStatus.REWORK
                || order.getStatus() == ProductionOrderStatus.APPROVED
                || order.getStatus() == ProductionOrderStatus.REJECTED) {
            return order.getStatus();
        }

        if (order.getEstimatedTotalTime() == null) {
            return ProductionOrderStatus.PLANNED;
        }

        long worked = getWorkedMinutes(order);
        if (worked >= order.getEstimatedTotalTime()) {
            return ProductionOrderStatus.FINISHED_BY_TIME;
        }

        return ProductionOrderStatus.IN_PROGRESS;
    }
}
