package Kizuna_core_service.qualityInspection.controller;

import Kizuna_core_service.qualityInspection.dto.QualityInspectionRequestDto;
import Kizuna_core_service.qualityInspection.dto.QualityInspectionResponseDto;
import Kizuna_core_service.qualityInspection.service.QualityInspectionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@AllArgsConstructor
@RestController
@RequestMapping("/quality-inspection")
public class QualityInspectionController {

    private final QualityInspectionService qualityInspectionService;


    @GetMapping
    public List<QualityInspectionResponseDto> findAll() {
        return qualityInspectionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualityInspectionResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(qualityInspectionService.findById(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<QualityInspectionResponseDto> create(@Valid @RequestBody QualityInspectionRequestDto requestDto) {
        QualityInspectionResponseDto response = qualityInspectionService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
