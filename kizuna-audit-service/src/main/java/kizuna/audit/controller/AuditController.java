package kizuna.audit.controller;

import kizuna.audit.domain.Audit;
import kizuna.audit.repository.AuditRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit")
@AllArgsConstructor
public class AuditController {

    private final AuditRepository auditRepository;



    @GetMapping
    public ResponseEntity<Page<Audit>> getAll(@PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok().body(auditRepository.findAll(pageable));
    }

    @GetMapping("/entity/{entity}")
    public ResponseEntity<Page<Audit>> findByEntity(@PageableDefault(size = 10,sort = "timestamp",direction = Sort.Direction.DESC) Pageable pageable, @PathVariable String entity ) {
        return ResponseEntity.ok().body(auditRepository.findByEntity(entity,pageable));
    }

    @GetMapping("/entity-id/{id}")
    public ResponseEntity<Page<Audit>> findByEntityId(@PageableDefault(size = 10,sort = "timestamp",direction = Sort.Direction.DESC) Pageable pageable,@PathVariable String id) {
        return ResponseEntity.ok().body(auditRepository.findByEntityId(id,pageable));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<Audit>> findByUsername(@PageableDefault(size = 10,sort = "timestamp",direction = Sort.Direction.DESC) Pageable pageable,@PathVariable String username) {
        return ResponseEntity.ok().body(auditRepository.findByUsername(username,pageable));
    }
}