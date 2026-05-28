package kizuna.audit.consumer;

import kizuna.audit.domain.Audit;
import kizuna.audit.dto.AuditEventDto;
import kizuna.audit.repository.AuditRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DlqReprocessor {

    private final AuditRepository auditRepository;

    public DlqReprocessor(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @RabbitListener(queues = "audit.dlq")
    public void reprocess(@Payload AuditEventDto event) {
        try {
            System.out.println("🔁 Tentando reprocessar evento DLQ...");

            Audit audit = Audit.builder()
                    .action(event.action())
                    .entity(event.entity())
                    .entityId(event.entityId())
                    .userId(event.userId())
                    .username(event.username())
                    .timestamp(event.timestamp())
                    .details((Map<String, Object>) event.data())
                    .build();

            auditRepository.save(audit);

            System.out.println("✅ Evento reprocessado com sucesso!");

        } catch (Exception e) {
            System.err.println("❌ Falha ao reprocessar DLQ. Evento descartado: " + event);
        }
    }
}
