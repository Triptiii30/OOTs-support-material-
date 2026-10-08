package com.smart.manufacturing.strategy;

import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Priority Strategy and DSA PriorityQueue Tests")
class PriorityStrategyTest {

    private PriorityStrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        List<PriorityStrategy> strategies = List.of(
                new LowPriorityStrategy(),
                new NormalPriorityStrategy(),
                new HighPriorityStrategy(),
                new UrgentPriorityStrategy()
        );
        strategyFactory = new PriorityStrategyFactory(strategies);
    }

    @Test
    @DisplayName("Strategy Factory returns correct polymorphic strategy instances")
    void testStrategyFactoryMapping() {
        assertEquals(1000, strategyFactory.getStrategy(Priority.URGENT).getPriorityWeight());
        assertEquals(500, strategyFactory.getStrategy(Priority.HIGH).getPriorityWeight());
        assertEquals(100, strategyFactory.getStrategy(Priority.NORMAL).getPriorityWeight());
        assertEquals(10, strategyFactory.getStrategy(Priority.LOW).getPriorityWeight());
    }

    @Test
    @DisplayName("Polymorphic strategies calculate expedited surcharges and shift lead times")
    void testSurchargesAndLeadTimes() {
        BigDecimal base = BigDecimal.valueOf(1000.00);

        // Urgent: 15% surcharge, compressed 24/7 lead time
        PriorityStrategy urgent = strategyFactory.getStrategy(Priority.URGENT);
        assertEquals(new BigDecimal("150.00"), urgent.calculateExpeditedSurcharge(base));
        assertEquals(1, urgent.calculateLeadTimeDays(24)); // 24 hours / 24 = 1 day

        // Normal: 0% surcharge, 8 hrs/day
        PriorityStrategy normal = strategyFactory.getStrategy(Priority.NORMAL);
        assertEquals(BigDecimal.ZERO, normal.calculateExpeditedSurcharge(base));
        assertEquals(3, normal.calculateLeadTimeDays(24)); // 24 hours / 8 = 3 days
    }

    @Test
    @DisplayName("PriorityQueue processes URGENT before HIGH, NORMAL, and LOW")
    void testPriorityQueueOrderingByPriority() {
        OrderSchedulingComparator comparator = new OrderSchedulingComparator(strategyFactory);
        PriorityQueue<ManufacturingOrder> queue = new PriorityQueue<>(comparator);

        ManufacturingOrder normalOrder = new ManufacturingOrder("ORD-NORM", null, LocalDate.now(), LocalDate.now().plusDays(5), Priority.NORMAL, null);
        ManufacturingOrder urgentOrder = new ManufacturingOrder("ORD-URG", null, LocalDate.now(), LocalDate.now().plusDays(5), Priority.URGENT, null);
        ManufacturingOrder lowOrder = new ManufacturingOrder("ORD-LOW", null, LocalDate.now(), LocalDate.now().plusDays(5), Priority.LOW, null);
        ManufacturingOrder highOrder = new ManufacturingOrder("ORD-HIGH", null, LocalDate.now(), LocalDate.now().plusDays(5), Priority.HIGH, null);

        queue.offer(normalOrder);
        queue.offer(lowOrder);
        queue.offer(urgentOrder);
        queue.offer(highOrder);

        // Queue poll sequence must strictly follow URGENT -> HIGH -> NORMAL -> LOW
        assertEquals("ORD-URG", queue.poll().getOrderNumber());
        assertEquals("ORD-HIGH", queue.poll().getOrderNumber());
        assertEquals("ORD-NORM", queue.poll().getOrderNumber());
        assertEquals("ORD-LOW", queue.poll().getOrderNumber());
    }

    @Test
    @DisplayName("PriorityQueue breaks priority ties using earlier delivery deadline")
    void testPriorityQueueTieBreakingByDeadline() {
        OrderSchedulingComparator comparator = new OrderSchedulingComparator(strategyFactory);
        PriorityQueue<ManufacturingOrder> queue = new PriorityQueue<>(comparator);

        LocalDate today = LocalDate.now();
        ManufacturingOrder laterDeadline = new ManufacturingOrder("ORD-LATER", null, today, today.plusDays(10), Priority.HIGH, null);
        ManufacturingOrder earlierDeadline = new ManufacturingOrder("ORD-EARLIER", null, today, today.plusDays(2), Priority.HIGH, null);

        queue.offer(laterDeadline);
        queue.offer(earlierDeadline);

        assertEquals("ORD-EARLIER", queue.poll().getOrderNumber());
        assertEquals("ORD-LATER", queue.poll().getOrderNumber());
    }
}
