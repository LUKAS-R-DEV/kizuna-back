package kizuna_notification_service.monitoring.monitor.config;

import kizuna_notification_service.monitoring.monitor.domain.Monitored;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "monitor")
public class ConfigMonitor {
    private List<Monitored> services = new ArrayList<>();
}
