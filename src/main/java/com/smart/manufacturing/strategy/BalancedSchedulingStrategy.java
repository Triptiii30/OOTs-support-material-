package com.smart.manufacturing.strategy;

import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.enums.Priority;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class BalancedSchedulingStrategy implements SchedulingStrategy {

    @Override
    public PriorityQueue<ManufacturingOrder> schedule(List<ManufacturingOrder> activeOrders) {
        Comparator<ManufacturingOrder> cmp =
                Comparator.comparingDouble((ManufacturingOrder o) -> -score(o));
        PriorityQueue<ManufacturingOrder> queue =
                new PriorityQueue<>(Math.max(1, activeOrders.size()), cmp);
        activeOrders.forEach(queue::add);
        return queue;
    }

    private double score(ManufacturingOrder o) {
        int w = switch (o.getPriority() != null ? o.getPriority() : Priority.NORMAL) {
            case URGENT -> 4; case HIGH -> 3; case NORMAL -> 2; case LOW -> 1;
        };
        long days = o.getRequiredDate() != null
                ? Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), o.getRequiredDate()))
                : 365L;
        return (double) w * (365.0 / days);
    }

    @Override public String getStrategyName() { return "Balanced Priority-Deadline Scheduling"; }
    @Override public String getDescription() { return "Weighted priority × deadline urgency"; }
}
