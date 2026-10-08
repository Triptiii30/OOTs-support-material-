package com.smart.manufacturing.controller.api;

import com.smart.manufacturing.dto.ApiResponse;
import com.smart.manufacturing.dto.DashboardStatsDto;
import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {

    private final ReportService reportService;
    private final OrderService orderService;

    @Autowired
    public ReportApiController(ReportService reportService, OrderService orderService) {
        this.reportService = reportService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = reportService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Dashboard statistics retrieved", stats));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersReport(ReportFilterDto filter) {
        List<ManufacturingOrder> orders = reportService.getFilteredOrdersReport(filter);
        List<OrderResponseDto> dtos = orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Orders report retrieved", dtos));
    }

    @GetMapping("/production")
    public ResponseEntity<ApiResponse<List<ProductionTask>>> getProductionReport() {
        List<ProductionTask> tasks = reportService.getProductionReport();
        return ResponseEntity.ok(ApiResponse.ok("Production report retrieved", tasks));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<Inventory>>> getInventoryReport() {
        List<Inventory> inventoryList = reportService.getInventoryReport();
        return ResponseEntity.ok(ApiResponse.ok("Inventory report retrieved", inventoryList));
    }

    @GetMapping("/orders/csv")
    public ResponseEntity<String> exportOrdersCsv(ReportFilterDto filter) {
        String csv = reportService.exportOrdersCsv(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/inventory/csv")
    public ResponseEntity<String> exportInventoryCsv() {
        String csv = reportService.exportInventoryCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/production/csv")
    public ResponseEntity<String> exportProductionCsv() {
        String csv = reportService.exportProductionCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"production-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
