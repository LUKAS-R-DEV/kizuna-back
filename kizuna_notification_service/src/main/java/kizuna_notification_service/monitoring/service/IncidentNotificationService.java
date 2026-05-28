package kizuna_notification_service.monitoring.service;

import kizuna_notification_service.monitoring.incident.domain.Incident;
import kizuna_notification_service.monitoring.incident.repository.IncidentRepository;
import kizuna_notification_service.monitoring.mail.HealthAlertEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentNotificationService {

    private final HealthAlertEmailService healthAlertEmailService;
    private final IncidentRepository incidentRepository;

    @Async("healthAlertTaskExecutor")
    public void notifyIfNeeded(Incident incident) {
        if (incident == null || incident.getId() == null) {
            log.warn("[MAIL] Cannot notify — incident without id");
            return;
        }
        Incident current = incidentRepository.findById(incident.getId()).orElse(incident);
        if (current.isNotificationSent()) {
            return;
        }
        boolean sent = healthAlertEmailService.sendIncidentAlert(current);
        if (sent) {
            current.setNotificationSent(true);
            incidentRepository.save(current);
        }
    }
}
