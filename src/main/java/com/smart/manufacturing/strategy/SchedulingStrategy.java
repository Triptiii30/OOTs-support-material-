package com.smart.manufacturing.strategy;

import com.smart.manufacturing.entity.ManufacturingOrder;
import java.util.List;
import java.util.PriorityQueue;

/**
 * SchedulingStrategy — Interface for polymorphic scheduling.
 * Demonstrates: Interface, Abstraction, Realization, Polymorphism.
 */
public interface SchedulingStrategy {
    PriorityQueue<ManufacturingOrder> schedule(List<ManufacturingOrder> activeOrders);
    String getStrategyName();
    String getDescription();
}
