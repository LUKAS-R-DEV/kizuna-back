package kizuna.audit.repository;

import kizuna.audit.domain.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditRepository  extends MongoRepository<Audit, String> {
    Page<Audit> findByEntity(String entity, Pageable pageable);
    Page<Audit> findByEntityId(String entityId, Pageable pageable);
    Page<Audit> findByUsername(String username, Pageable pageable);
}
