package com.smart.manufacturing.strategy;

import com.smart.manufacturing.entity.ManufacturingOrder;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class DeadlineSchedulingStrategy implements SchedulingStrategy {

    @Override
    public PriorityQueue<ManufacturingOrder> schedule(List<ManufacturingOrder> activeOrders) {
        Comparator<ManufacturingOrder> cmp =
                Comparator.comparing(o -> o.getRequiredDate() != null ? o.getRequiredDate() : LocalDate.MAX);
        PriorityQueue<ManufacturingOrder> queue =
                new PriorityQueue<>(Math.max(1, activeOrders.size()), cmp);
        activeOrders.stream()
                .filter(o -> o.getRequiredDate() != null)
                .forEach(queue::add);
        return queue;
    }

    @Override public String getStrategyName() { return "Deadline-Based Scheduling (EDF)"; }
    @Override public String getDescription() { return "Earliest Deadline First"; }
}
