package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.OrderItemDto;
import com.smart.manufacturing.dto.OrderRequestDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.exception.InvalidOrderException;
import com.smart.manufacturing.exception.InvalidOrderStatusException;
import com.smart.manufacturing.repository.*;
import com.smart.manufacturing.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Unit Tests")
class OrderServiceTest {

    @Mock
    private ManufacturingOrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private SchedulingService schedulingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new Customer("CUST-1", "Apex Corp", "contact@apex.com", "+123", "Apex Corp", "HQ");
        customer.setId(10L);

        product = new Product("SKU-1", "Robotic Arm", "6-axis", "Robotics", BigDecimal.valueOf(1000.00), 10, 5);
        product.setId(20L);
    }

    @Test
    @DisplayName("Create order calculates subtotal, 8% VAT, and grand total correctly")
    void testCreateOrderCalculatesTotals() {
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(ManufacturingOrder.class))).thenAnswer(i -> {
            ManufacturingOrder o = i.getArgument(0);
            o.setId(101L);
            return o;
        });

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(10L);
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(7));
        dto.setPriority(Priority.HIGH);
        dto.setItems(List.of(new OrderItemDto(20L, 2))); // 2 * $1000 = $2000

        ManufacturingOrder result = orderService.createOrder(dto, "staff");

        assertNotNull(result);
        assertEquals(new BigDecimal("2000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("160.00"), result.getTaxAmount()); // 8% of 2000
        assertEquals(new BigDecimal("2160.00"), result.getGrandTotal());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        verify(statusHistoryRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Create order throws InvalidOrderException when item list is empty")
    void testCreateOrderWithoutItemsFails() {
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(10L);
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(7));
        dto.setItems(List.of());

        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(dto, "staff"));
    }

    @Test
    @DisplayName("Direct transition from CREATED to COMPLETED throws InvalidOrderStatusException")
    void testInvalidTransitionThrowsException() {
        ManufacturingOrder order = new ManufacturingOrder("ORD-1", customer, LocalDate.now(), LocalDate.now().plusDays(5), Priority.NORMAL, "");
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> orderService.completeOrder(1L, "admin"));
    }

    @Test
    @DisplayName("Order validation successfully transitions CREATED to VALIDATED status")
    void testValidateOrderLifecycle() {
        ManufacturingOrder order = new ManufacturingOrder("ORD-1", customer, LocalDate.now(), LocalDate.now().plusDays(5), Priority.NORMAL, "");
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.addItem(new com.smart.manufacturing.entity.OrderItem(order, product, 1, product.getUnitPrice()));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(ManufacturingOrder.class))).thenAnswer(i -> i.getArgument(0));

        ManufacturingOrder validated = orderService.validateOrder(1L, "staff");

        assertEquals(OrderStatus.VALIDATED, validated.getStatus());
        verify(inventoryService, times(1)).validateStockForOrder(eq(20L), eq(1));
    }
}
