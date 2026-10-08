package com.smart.manufacturing.controller.api;

import com.smart.manufacturing.dto.ApiResponse;
import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ProductionTaskDto;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.enums.ProductionTaskStatus;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.ProductionService;
import com.smart.manufacturing.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/production")
public class ProductionApiController {

    private final ProductionService productionService;
    private final SchedulingService schedulingService;
    private final OrderService orderService;

    @Autowired
    public ProductionApiController(ProductionService productionService,
                                   SchedulingService schedulingService,
                                   OrderService orderService) {
        this.productionService = productionService;
        this.schedulingService = schedulingService;
        this.orderService = orderService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<ProductionTaskDto>>> getAllTasks() {
        List<ProductionTask> tasks = productionService.getAllTasks();
        List<ProductionTaskDto> dtos = tasks.stream()
                .map(productionService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Production tasks retrieved", dtos));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<ProductionTaskDto>> getTaskById(@PathVariable Long id) {
        ProductionTask task = productionService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.ok("Task found", productionService.convertToDto(task)));
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRODUCTION_MANAGER')")
    public ResponseEntity<ApiResponse<ProductionTaskDto>> updateTask(
            @PathVariable Long id,
            @RequestParam int progress,
            @RequestParam ProductionTaskStatus status,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ProductionTask updated = productionService.updateTaskProgress(id, progress, status, notes, username);
        return ResponseEntity.ok(ApiResponse.ok("Task updated successfully", productionService.convertToDto(updated)));
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getPrioritizedQueue() {
        List<ManufacturingOrder> queue = schedulingService.getPrioritizedProductionQueueList();
        List<OrderResponseDto> dtos = queue.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Prioritized production queue (DSA) retrieved", dtos));
    }
}
