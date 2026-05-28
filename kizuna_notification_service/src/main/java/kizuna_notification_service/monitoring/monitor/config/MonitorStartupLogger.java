package kizuna_notification_service.monitoring.monitor.config;

import kizuna_notification_service.monitoring.mail.HealthAlertMailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorStartupLogger {

    private final ConfigMonitor configMonitor;
    private final HealthAlertMailProperties mailProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void logMonitorReady() {
        int count = configMonitor.getServices() == null ? 0 : configMonitor.getServices().size();
        log.info("[MONITOR] Health scheduler active — {} services, mail enabled={}, recipients={}",
                count,
                mailProperties.isEnabled(),
                mailProperties.getRecipients().size());
    }
}
