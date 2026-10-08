package com.smart.manufacturing.strategy;

import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.enums.Priority;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class PrioritySchedulingStrategy implements SchedulingStrategy {

    @Override
    public PriorityQueue<ManufacturingOrder> schedule(List<ManufacturingOrder> activeOrders) {
        Comparator<ManufacturingOrder> comparator =
                Comparator.comparingInt(o -> -weight(o.getPriority()));
        PriorityQueue<ManufacturingOrder> queue =
                new PriorityQueue<>(Math.max(1, activeOrders.size()), comparator);
        activeOrders.stream()
                .filter(o -> o.getPriority() != null)
                .forEach(queue::add);
        return queue;
    }

    private int weight(Priority p) {
        return switch (p) {
            case URGENT -> 4; case HIGH -> 3; case NORMAL -> 2; case LOW -> 1;
        };
    }

    @Override public String getStrategyName() { return "Priority-Based Scheduling"; }
    @Override public String getDescription() { return "URGENT > HIGH > NORMAL > LOW"; }
}
