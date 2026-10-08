package com.smart.manufacturing.service;

import com.smart.manufacturing.entity.*;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.repository.ManufacturingOrderRepository;
import com.smart.manufacturing.repository.ProductionScheduleRepository;
import com.smart.manufacturing.repository.ProductionTaskRepository;
import com.smart.manufacturing.service.impl.SchedulingServiceImpl;
import com.smart.manufacturing.strategy.LowPriorityStrategy;
import com.smart.manufacturing.strategy.NormalPriorityStrategy;
import com.smart.manufacturing.strategy.PriorityStrategyFactory;
import com.smart.manufacturing.strategy.UrgentPriorityStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scheduling Service Unit Tests")
class SchedulingServiceTest {

    @Mock
    private ManufacturingOrderRepository orderRepository;

    @Mock
    private ProductionScheduleRepository scheduleRepository;

    @Mock
    private ProductionTaskRepository taskRepository;

    private SchedulingServiceImpl schedulingService;

    private ManufacturingOrder order;
    private Product product;

    @BeforeEach
    void setUp() {
        PriorityStrategyFactory strategyFactory = new PriorityStrategyFactory(List.of(
                new LowPriorityStrategy(),
                new NormalPriorityStrategy(),
                new UrgentPriorityStrategy()
        ));
        schedulingService = new SchedulingServiceImpl(orderRepository, scheduleRepository, taskRepository, strategyFactory);

        Customer customer = new Customer("C-1", "Test Co", "c@test.com", "123", "Test Co", "City");
        product = new Product("SKU-1", "CNC Part", "Desc", "Mechanical", BigDecimal.valueOf(100), 4, 10);
        order = new ManufacturingOrder("ORD-SCHED", customer, LocalDate.now(), LocalDate.now().plusDays(5), Priority.URGENT, "Rush");
        order.setId(50L);
        order.addItem(new OrderItem(order, product, 2, product.getUnitPrice()));
    }

    @Test
    @DisplayName("Scheduling an order creates production tasks and schedule batch")
    void testScheduleOrderCreatesTasks() {
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(scheduleRepository.findByScheduleCode(any())).thenReturn(Optional.empty());
        when(scheduleRepository.save(any(ProductionSchedule.class))).thenAnswer(i -> i.getArgument(0));

        ProductionSchedule schedule = schedulingService.scheduleOrder(50L, "manager");

        assertNotNull(schedule);
        verify(taskRepository, atLeastOnce()).save(any(ProductionTask.class));
    }

    @Test
    @DisplayName("Prioritized production queue returns non-empty PriorityQueue of active orders")
    void testBuildPrioritizedProductionQueue() {
        order.setStatus(OrderStatus.APPROVED);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        PriorityQueue<ManufacturingOrder> queue = schedulingService.buildPrioritizedProductionQueue();

        assertFalse(queue.isEmpty());
        assertEquals("ORD-SCHED", queue.peek().getOrderNumber());
    }
}
