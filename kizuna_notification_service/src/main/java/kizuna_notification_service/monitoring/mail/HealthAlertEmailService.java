package kizuna_notification_service.monitoring.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kizuna_notification_service.monitoring.incident.domain.Incident;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAlertEmailService {

    private final JavaMailSender mailSender;
    private final HealthAlertMailProperties properties;

    public boolean sendIncidentAlert(Incident incident) {
        if (!properties.isConfigured()) {
            log.warn("[MAIL] Alert not sent for {} — enabled={}, recipients={}",
                    incident.getServiceName(),
                    properties.isEnabled(),
                    properties.getRecipients().size());
            return false;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(properties.getFrom());
            helper.setTo(properties.getRecipients().toArray(String[]::new));
            helper.setSubject(HealthAlertEmailTemplate.buildSubject(incident));
            helper.setText(
                    HealthAlertEmailTemplate.buildPlainText(incident),
                    HealthAlertEmailTemplate.buildHtml(incident)
            );

            mailSender.send(mimeMessage);
            log.info("[MAIL] Health alert sent for {} ({})", incident.getServiceName(), incident.getStatus());
            return true;
        } catch (MessagingException e) {
            log.error("[MAIL] Failed to send health alert for {}: {}", incident.getServiceName(), e.getMessage());
            return false;
        }
    }
}
