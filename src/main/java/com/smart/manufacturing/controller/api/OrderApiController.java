package com.smart.manufacturing.controller.api;

import com.smart.manufacturing.dto.ApiResponse;
import com.smart.manufacturing.dto.OrderRequestDto;
import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    @Autowired
    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getAllOrders(ReportFilterDto filter) {
        List<ManufacturingOrder> orders = orderService.filterOrders(filter);
        List<OrderResponseDto> dtos = orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Orders retrieved successfully", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable Long id) {
        ManufacturingOrder order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order found", orderService.convertToDto(order)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORDER_STAFF')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(@Valid @RequestBody OrderRequestDto dto,
                                                                    Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ManufacturingOrder created = orderService.createOrder(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully", orderService.convertToDto(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORDER_STAFF')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrder(@PathVariable Long id,
                                                                    @Valid @RequestBody OrderRequestDto dto,
                                                                    Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ManufacturingOrder updated = orderService.updateOrder(id, dto, username);
        return ResponseEntity.ok(ApiResponse.ok("Order updated successfully", orderService.convertToDto(updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORDER_STAFF')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id,
                                                         @RequestParam(required = false) String reason,
                                                         Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        orderService.cancelOrder(id, reason, username);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled successfully", null));
    }

    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORDER_STAFF', 'ROLE_PRODUCTION_MANAGER')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> validateOrder(@PathVariable Long id,
                                                                       Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ManufacturingOrder validated = orderService.validateOrder(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Order validated successfully", orderService.convertToDto(validated)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRODUCTION_MANAGER')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> approveOrder(@PathVariable Long id,
                                                                      Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ManufacturingOrder approved = orderService.approveOrder(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Order approved and inventory reserved", orderService.convertToDto(approved)));
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRODUCTION_MANAGER')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> scheduleOrder(@PathVariable Long id,
                                                                       Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ManufacturingOrder scheduled = orderService.scheduleOrder(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Order scheduled for production", orderService.convertToDto(scheduled)));
    }

    @PostMapping("/{id}/start-production")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRODUCTION_MANAGER')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> startProduction(@PathVariable Long id,
                                                                         Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ManufacturingOrder inProgress = orderService.startProduction(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Production started for order", orderService.convertToDto(inProgress)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRODUCTION_MANAGER')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> completeOrder(@PathVariable Long id,
                                                                       Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        ManufacturingOrder completed = orderService.completeOrder(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Order and production completed successfully", orderService.convertToDto(completed)));
    }
}
