package com.smart.manufacturing.integration;

import com.smart.manufacturing.dto.OrderItemDto;
import com.smart.manufacturing.dto.OrderRequestDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.repository.CustomerRepository;
import com.smart.manufacturing.repository.ManufacturingOrderRepository;
import com.smart.manufacturing.repository.ProductRepository;
import com.smart.manufacturing.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("End-to-End Order Lifecycle Integration Test")
class OrderLifecycleIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ManufacturingOrderRepository orderRepository;

    @Test
    @DisplayName("Complete workflow: Create -> Validate -> Approve -> Schedule -> InProgress -> Complete")
    void testCompleteOrderWorkflow() {
        // 1. Fetch seed customer and product
        Customer customer = customerRepository.findAll().stream().findFirst().orElseThrow();
        Product product = productRepository.findAll().stream().findFirst().orElseThrow();

        // 2. Create Order
        OrderRequestDto request = new OrderRequestDto();
        request.setCustomerId(customer.getId());
        request.setRequiredDeliveryDate(LocalDate.now().plusDays(10));
        request.setPriority(Priority.HIGH);
        request.setNotes("Integration test rush order");
        request.setItems(List.of(new OrderItemDto(product.getId(), 2)));

        ManufacturingOrder createdOrder = orderService.createOrder(request, "admin");
        assertNotNull(createdOrder.getId());
        assertEquals(OrderStatus.CREATED, createdOrder.getStatus());
        assertTrue(createdOrder.getGrandTotal().compareTo(BigDecimal.ZERO) > 0);

        Long orderId = createdOrder.getId();

        // 3. Validate
        ManufacturingOrder validatedOrder = orderService.validateOrder(orderId, "admin");
        assertEquals(OrderStatus.VALIDATED, validatedOrder.getStatus());

        // 4. Approve & Allocate
        ManufacturingOrder approvedOrder = orderService.approveOrder(orderId, "admin");
        assertEquals(OrderStatus.APPROVED, approvedOrder.getStatus());

        // 5. Schedule Production Tasks
        ManufacturingOrder scheduledOrder = orderService.scheduleOrder(orderId, "admin");
        assertEquals(OrderStatus.SCHEDULED, scheduledOrder.getStatus());
        assertFalse(scheduledOrder.getTasks().isEmpty());

        // 6. Start Production
        ManufacturingOrder inProgressOrder = orderService.startProduction(orderId, "admin");
        assertEquals(OrderStatus.IN_PROGRESS, inProgressOrder.getStatus());

        // 7. Complete Order & Fulfill Stock
        ManufacturingOrder completedOrder = orderService.completeOrder(orderId, "admin");
        assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());

        // Verify status audit history chain
        ManufacturingOrder reloaded = orderRepository.findById(orderId).orElseThrow();
        assertTrue(reloaded.getStatusHistory().size() >= 5);
    }
}
