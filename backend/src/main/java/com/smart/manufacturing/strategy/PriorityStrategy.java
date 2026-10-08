package com.smart.manufacturing.strategy;

import com.smart.manufacturing.enums.Priority;
import java.math.BigDecimal;

/**
 * Strategy interface demonstrating OOP Polymorphism.
 * Defines algorithms for priority weighting, lead time calculation, and expediting surcharges.
 */
public interface PriorityStrategy {

    Priority getPriority();

    /**
     * Numerical scheduling weight for sorting and DSA PriorityQueue operations.
     */
    int getPriorityWeight();

    /**
     * Calculates expedited surcharge for rush orders.
     */
    BigDecimal calculateExpeditedSurcharge(BigDecimal baseSubtotal);

    /**
     * Estimates lead time in days depending on work shift allocation for this priority level.
     */
    int calculateLeadTimeDays(int totalDurationHours);

    /**
     * Human-readable manufacturing scheduling policy.
     */
    String getSchedulingPolicyDescription();
}
