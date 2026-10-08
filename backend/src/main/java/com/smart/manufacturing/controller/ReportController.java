package com.smart.manufacturing.controller;

import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.service.CustomerService;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final OrderService orderService;
    private final CustomerService customerService;

    @Autowired
    public ReportController(ReportService reportService, OrderService orderService, CustomerService customerService) {
        this.reportService = reportService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @GetMapping
    public String viewReports(ReportFilterDto filter, Model model) {
        List<ManufacturingOrder> orders = reportService.getFilteredOrdersReport(filter);
        List<OrderResponseDto> orderDtos = orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());

        List<ProductionTask> tasks = reportService.getProductionReport();
        List<Inventory> inventories = reportService.getInventoryReport();
        List<Customer> customers = reportService.getCustomerReport();

        model.addAttribute("orders", orderDtos);
        model.addAttribute("tasks", tasks);
        model.addAttribute("inventories", inventories);
        model.addAttribute("customers", customers);
        model.addAttribute("filter", filter != null ? filter : new ReportFilterDto());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("priorities", Priority.values());

        return "reports/index";
    }

    @GetMapping("/orders/export")
    public void exportOrdersCsv(ReportFilterDto filter, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"manufacturing_orders.csv\"");
        String csv = reportService.exportOrdersCsv(filter);
        response.getWriter().write(csv);
    }

    @GetMapping("/inventory/export")
    public void exportInventoryCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"inventory_report.csv\"");
        String csv = reportService.exportInventoryCsv();
        response.getWriter().write(csv);
    }

    @GetMapping("/production/export")
    public void exportProductionCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"production_tasks.csv\"");
        String csv = reportService.exportProductionCsv();
        response.getWriter().write(csv);
    }

    @GetMapping("/customers/export")
    public void exportCustomersCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"customer_directory.csv\"");
        String csv = reportService.exportCustomersCsv();
        response.getWriter().write(csv);
    }
}
