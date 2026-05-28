package kizuna_notification_service.monitoring.incident.repository;

import kizuna_notification_service.monitoring.incident.domain.Incident;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IncidentRepository extends MongoRepository<Incident, String> {
    List<Incident> findByServiceNameOrderByTimestampDesc(String serviceName);
}
