package com.smart.manufacturing.strategy;

import com.smart.manufacturing.entity.ManufacturingOrder;
import java.util.Comparator;

/**
 * Custom Comparator used by the Production Scheduling PriorityQueue (DSA).
 * Evaluates orders based on Polymorphic Priority Weight, Delivery Deadline, and Order Date.
 */
public class OrderSchedulingComparator implements Comparator<ManufacturingOrder> {

    private final PriorityStrategyFactory strategyFactory;

    public OrderSchedulingComparator() {
        this.strategyFactory = new PriorityStrategyFactory();
    }

    public OrderSchedulingComparator(PriorityStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public int compare(ManufacturingOrder o1, ManufacturingOrder o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        // 1. Higher priority weight comes first (descending comparison)
        int weight1 = strategyFactory.getStrategy(o1.getPriority()).getPriorityWeight();
        int weight2 = strategyFactory.getStrategy(o2.getPriority()).getPriorityWeight();
        int weightComparison = Integer.compare(weight2, weight1);
        if (weightComparison != 0) {
            return weightComparison;
        }

        // 2. If priorities are equal, earlier deadline comes first (ascending comparison)
        if (o1.getRequiredDeliveryDate() != null && o2.getRequiredDeliveryDate() != null) {
            int deadlineComparison = o1.getRequiredDeliveryDate().compareTo(o2.getRequiredDeliveryDate());
            if (deadlineComparison != 0) {
                return deadlineComparison;
            }
        }

        // 3. FIFO fallback based on creation order date
        if (o1.getOrderDate() != null && o2.getOrderDate() != null) {
            int dateComparison = o1.getOrderDate().compareTo(o2.getOrderDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
        }

        // 4. Stable tie breaker by ID
        if (o1.getId() != null && o2.getId() != null) {
            return Long.compare(o1.getId(), o2.getId());
        }

        return 0;
    }
}
