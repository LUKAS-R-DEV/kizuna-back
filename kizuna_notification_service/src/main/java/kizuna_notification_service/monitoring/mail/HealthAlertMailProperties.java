package kizuna_notification_service.monitoring.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "kizuna.health-alert.mail")
public class HealthAlertMailProperties {

    private boolean enabled = false;
    private String from = "kizuna-alerts@localhost";
    private List<String> recipients = new ArrayList<>();

    public void setRecipientsCsv(String recipientsCsv) {
        if (recipientsCsv == null || recipientsCsv.isBlank()) {
            this.recipients = new ArrayList<>();
            return;
        }
        this.recipients = Arrays.stream(recipientsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public boolean isConfigured() {
        return enabled && recipients != null && !recipients.isEmpty();
    }
}
