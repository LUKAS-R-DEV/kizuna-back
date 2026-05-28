package Kizuna_core_service.productionOrder.controller;

import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.dto.ProductionOrderRequestDto;
import Kizuna_core_service.productionOrder.dto.ProductionOrderResponseDto;
import Kizuna_core_service.productionOrder.service.ProductionOrderService;
import Kizuna_core_service.shared.dto.ApiResponseGeneric;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/production-order")
public class ProductionOrderController {
    private final ProductionOrderService productionOrderService;
    public ProductionOrderController(ProductionOrderService productionOrderService) {
        this.productionOrderService = productionOrderService;
    }
    @PreAuthorize("hasAnyAuthority('ROLE_PLANNER','ROLE_OPERATOR','ROLE_ADMIN')")
    @GetMapping
    public List<ProductionOrderResponseDto> findAll(){
        return productionOrderService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_PLANNER','ROLE_ADMIN')")
    @GetMapping("/status/{status}")
    public List<ProductionOrderResponseDto> findByStatus(@PathVariable ProductionOrderStatus status){
        return productionOrderService.findByStatus(status);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_PLANNER','ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductionOrderResponseDto> findById(@PathVariable Long id){
        return ResponseEntity.ok(productionOrderService.findById(id));
    }


    @PreAuthorize("hasAnyAuthority('ROLE_PLANNER','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductionOrderResponseDto> create(@Valid @RequestBody ProductionOrderRequestDto requestDto){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productionOrderResponseDto);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR','ROLE_ADMIN')")
    @PostMapping("/{id}/start")
    public ResponseEntity<ProductionOrderResponseDto> start(@PathVariable Long id){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.start(id);
        return ResponseEntity.ok(productionOrderResponseDto);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR','ROLE_ADMIN')")
    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponseGeneric> pause(@PathVariable Long id){
        ApiResponseGeneric apiResponseGeneric = productionOrderService.pause(id);
        return ResponseEntity.ok(apiResponseGeneric);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR','ROLE_ADMIN')")
    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponseGeneric> resume(@PathVariable Long id){
        ApiResponseGeneric apiResponseGeneric = productionOrderService.resume(id);
        return ResponseEntity.ok(apiResponseGeneric);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR','ROLE_ADMIN')")
    @PostMapping("/{id}/rework")
    public ResponseEntity<ApiResponseGeneric> rework(@PathVariable Long id){;
        ApiResponseGeneric apiResponseGeneric=productionOrderService.reworkProductionOrder(id);
        return ResponseEntity.ok(apiResponseGeneric);

    }
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR','ROLE_ADMIN')")
    @GetMapping("/status/WAITING_INSPECTION")
    public List<ProductionOrderResponseDto> findByStatusWaitingInspection(){
        return productionOrderService.findByStatus(ProductionOrderStatus.WAITING_INSPECTION);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR','ROLE_ADMIN')")
    @PostMapping("/{id}/finish")
    public ResponseEntity<ProductionOrderResponseDto> finish(@PathVariable Long id){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.finish(id);
        return ResponseEntity.ok(productionOrderResponseDto);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_PLANNER','ROLE_ADMIN')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ProductionOrderResponseDto> cancel(@PathVariable Long id){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.cancel(id);
        return ResponseEntity.ok(productionOrderResponseDto);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_PLANNER','ROLE_EXECUTIVE','ROLE_ADMIN')")
    @GetMapping("/queue")
    public List<ProductionOrderResponseDto> getQueue(){
        return productionOrderService.findByStatus(ProductionOrderStatus.PLANNED);
    }
    @PreAuthorize("hasAnyAuthority('ROLE_PLANNER','ROLE_ADMIN')")
    @PutMapping("/{id}/reorder")
    public void reorder(@PathVariable Long id, @RequestParam int position) {
        productionOrderService.moveOrder(id, position);
    }
}
