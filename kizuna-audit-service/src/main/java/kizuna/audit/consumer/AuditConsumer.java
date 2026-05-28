package kizuna.audit.consumer;

import kizuna.audit.domain.Audit;
import kizuna.audit.dto.AuditEventDto;
import kizuna.audit.repository.AuditRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditConsumer {

    private final AuditRepository auditRepository;

    public AuditConsumer(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @RabbitListener(queues = "audit.queue")
    public void consume(@Payload AuditEventDto event) {
        try {
            Audit audit = Audit.builder()
                    .action(event.action())
                    .entity(event.entity())
                    .entityId(event.entityId())
                    .username(event.username())
                    .userId(event.userId())
                    .timestamp(event.timestamp())
                    .details((Map<String, Object>) event.data())
                    .build();

            auditRepository.save(audit);
            System.out.println("Evento salvo: " + audit);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar evento de auditoria", e);
        }
    }

}