package kizuna.audit.service;

import kizuna.audit.domain.Audit;
import kizuna.audit.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void saveAudit(Map<String, Object> event) {

        Audit log = Audit.builder()
                .action((String) event.get("action"))
                .entity((String) event.get("entity"))
                .entityId((String) event.get("entityId"))
                .username((String) event.get("username"))
                .userId((String) event.get("userId"))
                .timestamp(LocalDateTime.parse((String) event.get("timestamp")))
                .details((Map<String, Object>) event.get("data"))
                .build();

        auditRepository.save(log);
    }
}