package kizuna_notification_service.monitoring.controller;

import kizuna_notification_service.monitoring.incident.domain.Incident;
import kizuna_notification_service.monitoring.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-services")
@RequiredArgsConstructor
public class HealthController {

    private final IncidentRepository incidentRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EXECUTIVE')")
    public ResponseEntity<Page<Incident>> getAllIncidents(
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(incidentRepository.findAll(pageable));
    }
}
