package com.smart.manufacturing.strategy;

import com.smart.manufacturing.enums.Priority;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class NormalPriorityStrategy implements PriorityStrategy {

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public int getPriorityWeight() {
        return 100;
    }

    @Override
    public BigDecimal calculateExpeditedSurcharge(BigDecimal baseSubtotal) {
        return BigDecimal.ZERO;
    }

    @Override
    public int calculateLeadTimeDays(int totalDurationHours) {
        // Standard single-shift manufacturing (8 hours per work day)
        return Math.max(1, (int) Math.ceil((double) totalDurationHours / 8.0));
    }

    @Override
    public String getSchedulingPolicyDescription() {
        return "Normal priority: standard shop floor single-shift queue (8 hrs/day allocation).";
    }
}
