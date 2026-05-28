package kizuna_notification_service.monitoring.monitor.config;

import kizuna_notification_service.monitoring.incident.domain.Incident;
import kizuna_notification_service.monitoring.incident.domain.Status;
import kizuna_notification_service.monitoring.incident.repository.IncidentRepository;
import kizuna_notification_service.monitoring.monitor.domain.Monitored;
import kizuna_notification_service.monitoring.service.HealthMonitorService;
import kizuna_notification_service.monitoring.service.IncidentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckScheduler {

    private final HealthMonitorService healthMonitorService;
    private final ConfigMonitor configMonitor;
    private final IncidentRepository incidentRepository;
    private final IncidentNotificationService incidentNotificationService;

    private final Map<String, String> servicesStatusMap = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 120_000, initialDelay = 30_000)
    public void checkServicesHealth() {
        List<Monitored> services = configMonitor.getServices();
        if (services == null || services.isEmpty()) {
            log.warn("[MONITOR] Nenhum serviço configurado em monitor.services");
            return;
        }

        Flux.fromIterable(services)
                .flatMap(service -> healthMonitorService
                        .checkHealth(service.getUrl())
                        .map(isUp -> Map.entry(service, isUp)))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(entry -> handleStatusChange(entry.getKey(), entry.getValue()))
                .doOnError(ex -> log.error("[MONITOR] Health check cycle failed: {}", ex.getMessage()))
                .onErrorComplete()
                .subscribe();
    }

    private void handleStatusChange(Monitored service, boolean isUp) {
        Status currentStatus = isUp ? Status.UP : Status.DOWN;
        String currentLabel = currentStatus.name();
        String previousStatus = servicesStatusMap.get(service.getName());

        if (previousStatus == null) {
            servicesStatusMap.put(service.getName(), currentLabel);
            log.info("[MONITOR] {} baseline: {}", service.getName(), currentLabel);
            if (currentStatus == Status.DOWN) {
                recordIncidentAndNotify(service, currentStatus, "INITIAL", currentLabel);
            }
            return;
        }

        if (!previousStatus.equals(currentLabel)) {
            recordIncidentAndNotify(service, currentStatus, previousStatus, currentLabel);
        }
    }

    private void recordIncidentAndNotify(
            Monitored service,
            Status currentStatus,
            String previousStatus,
            String currentLabel
    ) {
        Incident incident = Incident.builder()
                .serviceName(service.getName())
                .status(currentStatus)
                .message("Service changed status from " + previousStatus + " to " + currentLabel)
                .timestamp(LocalDateTime.now())
                .notificationSent(false)
                .build();

        Incident saved = incidentRepository.save(incident);
        incidentNotificationService.notifyIfNeeded(saved);
        servicesStatusMap.put(service.getName(), currentStatus.name());
        log.warn("[MONITOR] Incident recorded: {} -> {}", service.getName(), currentStatus);
    }
}
