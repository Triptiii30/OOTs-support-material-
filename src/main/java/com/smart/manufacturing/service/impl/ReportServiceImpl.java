package com.smart.manufacturing.service.impl;

import com.smart.manufacturing.dto.DashboardStatsDto;
import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.*;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.repository.*;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.ReportService;
import com.smart.manufacturing.service.SchedulingService;
import com.smart.manufacturing.util.CsvExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ManufacturingOrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductionTaskRepository taskRepository;
    private final OrderService orderService;
    private final SchedulingService schedulingService;

    @Autowired
    public ReportServiceImpl(CustomerRepository customerRepository,
                             ProductRepository productRepository,
                             ManufacturingOrderRepository orderRepository,
                             InventoryRepository inventoryRepository,
                             ProductionTaskRepository taskRepository,
                             @Lazy OrderService orderService,
                             @Lazy SchedulingService schedulingService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.taskRepository = taskRepository;
        this.orderService = orderService;
        this.schedulingService = schedulingService;
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalCustomers(customerRepository.count());
        stats.setTotalProducts(productRepository.count());
        stats.setTotalOrders(orderRepository.count());

        long pending = orderRepository.countByStatus(OrderStatus.CREATED) +
                       orderRepository.countByStatus(OrderStatus.VALIDATED) +
                       orderRepository.countByStatus(OrderStatus.APPROVED);
        stats.setPendingOrders(pending);

        long inProd = orderRepository.countByStatus(OrderStatus.SCHEDULED) +
                      orderRepository.countByStatus(OrderStatus.IN_PROGRESS);
        stats.setInProductionOrders(inProd);

        stats.setCompletedOrders(orderRepository.countByStatus(OrderStatus.COMPLETED));
        stats.setDelayedOrders(orderRepository.countDelayedOrders(LocalDate.now()));
        stats.setLowStockProducts(inventoryRepository.countLowStockInventories());
        stats.setHighPriorityOrders(orderRepository.countHighAndUrgentOrders());

        // Status breakdown Map
        Map<String, Long> statusMap = new HashMap<>();
        for (OrderStatus st : OrderStatus.values()) {
            statusMap.put(st.name(), orderRepository.countByStatus(st));
        }
        stats.setOrdersByStatus(statusMap);

        // Priority breakdown Map
        Map<String, Long> priorityMap = new HashMap<>();
        for (Priority p : Priority.values()) {
            priorityMap.put(p.name(), orderRepository.countByPriority(p));
        }
        stats.setOrdersByPriority(priorityMap);

        // Inventory Category Map
        Map<String, Long> catMap = productRepository.findAll().stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
        stats.setInventoryByCategory(catMap);

        // Recent orders
        List<OrderResponseDto> recent = orderRepository.findTop10ByOrderByIdDesc().stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
        stats.setRecentOrders(recent);

        // Priority queue orders from DSA PriorityQueue
        List<OrderResponseDto> priorityQueue = schedulingService.getPrioritizedProductionQueueList().stream()
                .limit(5)
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
        stats.setPriorityQueueOrders(priorityQueue);

        return stats;
    }

    @Override
    public List<ManufacturingOrder> getFilteredOrdersReport(ReportFilterDto filter) {
        return orderService.filterOrders(filter);
    }

    @Override
    public List<ProductionTask> getProductionReport() {
        return taskRepository.findAllByOrderByPlannedStartDateAsc();
    }

    @Override
    public List<Inventory> getInventoryReport() {
        return inventoryRepository.findAll();
    }

    @Override
    public List<Customer> getCustomerReport() {
        return customerRepository.findAll();
    }

    @Override
    public String exportOrdersCsv(ReportFilterDto filter) {
        List<ManufacturingOrder> orders = getFilteredOrdersReport(filter);
        return CsvExportUtil.exportOrdersToCsv(orders);
    }

    @Override
    public String exportInventoryCsv() {
        List<Inventory> inventories = getInventoryReport();
        return CsvExportUtil.exportInventoryToCsv(inventories);
    }

    @Override
    public String exportProductionCsv() {
        List<ProductionTask> tasks = getProductionReport();
        return CsvExportUtil.exportProductionTasksToCsv(tasks);
    }

    @Override
    public String exportCustomersCsv() {
        List<Customer> customers = getCustomerReport();
        return CsvExportUtil.exportCustomersToCsv(customers);
    }
}
