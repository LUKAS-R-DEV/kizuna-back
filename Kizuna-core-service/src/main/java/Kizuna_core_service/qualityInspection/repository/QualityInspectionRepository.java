package Kizuna_core_service.qualityInspection.repository;

import Kizuna_core_service.qualityInspection.domain.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {

}
