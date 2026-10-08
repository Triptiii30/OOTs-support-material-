package com.smart.manufacturing.service.impl;

import com.smart.manufacturing.dto.OrderItemDto;
import com.smart.manufacturing.dto.OrderRequestDto;
import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.*;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.enums.ProductionTaskStatus;
import com.smart.manufacturing.exception.*;
import com.smart.manufacturing.repository.*;
import com.smart.manufacturing.service.InventoryService;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final ManufacturingOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final InventoryService inventoryService;
    private final SchedulingService schedulingService;

    @Autowired
    public OrderServiceImpl(ManufacturingOrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            OrderStatusHistoryRepository statusHistoryRepository,
                            InventoryService inventoryService,
                            @Lazy SchedulingService schedulingService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.inventoryService = inventoryService;
        this.schedulingService = schedulingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturingOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturingOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturingOrder getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
    }

    @Override
    public ManufacturingOrder createOrder(OrderRequestDto requestDto, String username) {
        log.info("Creating order for customer ID: {} by user: {}", requestDto.getCustomerId(), username);

        // Rule 1: Customer must exist
        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(requestDto.getCustomerId()));

        if (!customer.isActive()) {
            throw new InvalidOrderException("Cannot create order for inactive customer: " + customer.getName());
        }

        // Rule 2: Order must contain at least one item
        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        // Rule 3: Delivery deadline cannot be in the past
        LocalDate today = LocalDate.now();
        if (requestDto.getRequiredDeliveryDate() == null || requestDto.getRequiredDeliveryDate().isBefore(today)) {
            throw new InvalidOrderException("Required delivery date cannot be before today (" + today + ")");
        }

        String orderNumber = generateOrderNumber();
        ManufacturingOrder order = new ManufacturingOrder(
                orderNumber,
                customer,
                today,
                requestDto.getRequiredDeliveryDate(),
                requestDto.getPriority(),
                requestDto.getNotes()
        );

        // Process line items
        for (OrderItemDto itemDto : requestDto.getItems()) {
            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new InvalidOrderException("Item quantity must be strictly greater than zero");
            }

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemDto.getProductId()));

            if (!product.isActive()) {
                throw new InvalidOrderException("Cannot order inactive product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem(order, product, itemDto.getQuantity(), product.getUnitPrice());
            order.addItem(orderItem);
        }

        ManufacturingOrder savedOrder = orderRepository.save(order);

        // Audit status history
        recordStatusHistory(savedOrder, null, OrderStatus.CREATED, "Order created in system", username);

        return savedOrder;
    }

    @Override
    public ManufacturingOrder updateOrder(Long id, OrderRequestDto requestDto, String username) {
        log.info("Updating order ID: {} by user: {}", id, username);
        ManufacturingOrder order = getOrderById(id);

        // Rule: Completed or Cancelled orders cannot be edited
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Cannot update an order in status: " + order.getStatus());
        }

        if (requestDto.getRequiredDeliveryDate() != null) {
            order.setRequiredDeliveryDate(requestDto.getRequiredDeliveryDate());
        }
        if (requestDto.getPriority() != null) {
            order.setPriority(requestDto.getPriority());
        }
        order.setNotes(requestDto.getNotes());

        // Re-process items if provided and order is still in CREATED/VALIDATED state
        if (order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.VALIDATED) {
            if (requestDto.getItems() != null && !requestDto.getItems().isEmpty()) {
                List<OrderItem> currentItems = new ArrayList<>(order.getItems());
                for (OrderItem item : currentItems) {
                    order.removeItem(item);
                }

                for (OrderItemDto itemDto : requestDto.getItems()) {
                    if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                        throw new InvalidOrderException("Item quantity must be greater than zero");
                    }
                    Product product = productRepository.findById(itemDto.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(itemDto.getProductId()));
                    if (!product.isActive()) {
                        throw new InvalidOrderException("Product is inactive: " + product.getName());
                    }
                    OrderItem item = new OrderItem(order, product, itemDto.getQuantity(), product.getUnitPrice());
                    order.addItem(item);
                }
            }
        }

        return orderRepository.save(order);
    }

    @Override
    public void cancelOrder(Long id, String reason, String username) {
        log.info("Cancelling order ID: {} with reason: {}", id, reason);
        ManufacturingOrder order = getOrderById(id);

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Completed orders cannot be cancelled");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        OrderStatus prev = order.getStatus();

        // If order was approved or in production, release any allocated stock
        if (prev == OrderStatus.APPROVED || prev == OrderStatus.SCHEDULED || prev == OrderStatus.IN_PROGRESS) {
            for (OrderItem item : order.getItems()) {
                inventoryService.releaseStockForOrder(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        order.getId(),
                        username != null ? username : "System"
                );
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        recordStatusHistory(order, prev, OrderStatus.CANCELLED,
                "Order cancelled. Reason: " + (reason != null ? reason : "Not specified"), username);
    }

    @Override
    public ManufacturingOrder validateOrder(Long id, String username) {
        log.info("Validating order ID: {}", id);
        ManufacturingOrder order = getOrderById(id);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStatusException("Order can only be validated from CREATED status");
        }

        if (order.getItems().isEmpty()) {
            throw new InvalidOrderException("Order contains no items");
        }

        // Validate inventory availability
        for (OrderItem item : order.getItems()) {
            inventoryService.validateStockForOrder(item.getProduct().getId(), item.getQuantity());
        }

        OrderStatus prev = order.getStatus();
        order.transitionTo(OrderStatus.VALIDATED);
        ManufacturingOrder saved = orderRepository.save(order);

        recordStatusHistory(saved, prev, OrderStatus.VALIDATED, "Order passed validation criteria and stock checks", username);
        return saved;
    }

    @Override
    public ManufacturingOrder approveOrder(Long id, String username) {
        log.info("Approving order ID: {}", id);
        ManufacturingOrder order = getOrderById(id);

        if (order.getStatus() != OrderStatus.VALIDATED) {
            throw new InvalidOrderStatusException("Order must be VALIDATED before approval");
        }

        // Allocate inventory stock
        for (OrderItem item : order.getItems()) {
            inventoryService.allocateStockForOrder(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    order.getId(),
                    username != null ? username : "System"
            );
        }

        OrderStatus prev = order.getStatus();
        order.transitionTo(OrderStatus.APPROVED);
        ManufacturingOrder saved = orderRepository.save(order);

        recordStatusHistory(saved, prev, OrderStatus.APPROVED, "Order approved and stock reserved", username);
        return saved;
    }

    @Override
    public ManufacturingOrder scheduleOrder(Long id, String username) {
        log.info("Scheduling order ID: {}", id);
        ManufacturingOrder order = getOrderById(id);

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new InvalidOrderStatusException("Order must be APPROVED before scheduling");
        }

        // Delegate to SchedulingService to generate production tasks
        schedulingService.scheduleOrder(order.getId(), username);

        OrderStatus prev = order.getStatus();
        order.transitionTo(OrderStatus.SCHEDULED);
        ManufacturingOrder saved = orderRepository.save(order);

        recordStatusHistory(saved, prev, OrderStatus.SCHEDULED, "Production tasks created and scheduled", username);
        return saved;
    }

    @Override
    public ManufacturingOrder startProduction(Long id, String username) {
        log.info("Starting production for order ID: {}", id);
        ManufacturingOrder order = getOrderById(id);

        if (order.getStatus() != OrderStatus.SCHEDULED) {
            throw new InvalidOrderStatusException("Order must be SCHEDULED before starting production");
        }

        OrderStatus prev = order.getStatus();
        order.transitionTo(OrderStatus.IN_PROGRESS);
        ManufacturingOrder saved = orderRepository.save(order);

        // Update task statuses
        for (ProductionTask task : order.getTasks()) {
            if (task.getStatus() == ProductionTaskStatus.SCHEDULED || task.getStatus() == ProductionTaskStatus.PENDING) {
                task.setStatus(ProductionTaskStatus.IN_PROGRESS);
            }
        }

        recordStatusHistory(saved, prev, OrderStatus.IN_PROGRESS, "Shop floor manufacturing commenced", username);
        return saved;
    }

    @Override
    public ManufacturingOrder completeOrder(Long id, String username) {
        log.info("Completing order ID: {}", id);
        ManufacturingOrder order = getOrderById(id);

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new InvalidOrderStatusException("Order must be IN_PROGRESS before marking completed");
        }

        // Fulfill allocated stock and mark finished
        for (OrderItem item : order.getItems()) {
            inventoryService.fulfillStockForOrder(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    order.getId(),
                    username != null ? username : "System"
            );
        }

        OrderStatus prev = order.getStatus();
        order.transitionTo(OrderStatus.COMPLETED);
        ManufacturingOrder saved = orderRepository.save(order);

        // Mark all tasks completed
        for (ProductionTask task : order.getTasks()) {
            task.updateProgress(100, ProductionTaskStatus.COMPLETED);
        }

        recordStatusHistory(saved, prev, OrderStatus.COMPLETED, "Manufacturing finished and stock fulfilled", username);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturingOrder> filterOrders(ReportFilterDto filter) {
        if (filter == null) {
            return orderRepository.findAll();
        }
        return orderRepository.filterOrders(
                filter.getQuery(),
                filter.getStatus(),
                filter.getPriority(),
                filter.getStartDate(),
                filter.getEndDate()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturingOrder> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturingOrder> getDelayedOrders() {
        return orderRepository.findDelayedOrders(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrders() {
        return orderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersByPriority(Priority priority) {
        return orderRepository.countByPriority(priority);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDelayedOrders() {
        return orderRepository.countDelayedOrders(LocalDate.now());
    }

    @Override
    public OrderResponseDto convertToDto(ManufacturingOrder order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getName());
            dto.setCustomerCompany(order.getCustomer().getCompany());
        }
        dto.setOrderDate(order.getOrderDate());
        dto.setRequiredDeliveryDate(order.getRequiredDeliveryDate());
        dto.setPriority(order.getPriority());
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setGrandTotal(order.getGrandTotal());
        dto.setNotes(order.getNotes());
        dto.setDelayed(order.isDelayed());
        dto.setTotalQuantity(order.getTotalQuantity());

        List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(item.getId());
            if (item.getProduct() != null) {
                itemDto.setProductId(item.getProduct().getId());
                itemDto.setProductCode(item.getProduct().getProductCode());
                itemDto.setProductName(item.getProduct().getName());
            }
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setSubtotal(item.getSubtotal());
            return itemDto;
        }).collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    private void recordStatusHistory(ManufacturingOrder order, OrderStatus prev, OrderStatus next,
                                     String comments, String username) {
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }
        OrderStatusHistory history = new OrderStatusHistory(order, prev, next, user, comments);
        statusHistoryRepository.save(history);
        order.addStatusHistory(history);
    }

    private String generateOrderNumber() {
        long count = orderRepository.count() + 1001;
        String candidate = "ORD-2026-" + count;
        while (orderRepository.existsByOrderNumber(candidate)) {
            count++;
            candidate = "ORD-2026-" + count;
        }
        return candidate;
    }
}
