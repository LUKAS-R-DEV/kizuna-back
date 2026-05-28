package kizuna_notification_service;

import kizuna_notification_service.monitoring.mail.HealthAlertMailProperties;
import kizuna_notification_service.monitoring.monitor.config.ConfigMonitor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties({ConfigMonitor.class, HealthAlertMailProperties.class})
@SpringBootApplication
public class KizunaNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(KizunaNotificationServiceApplication.class, args);
	}

}
