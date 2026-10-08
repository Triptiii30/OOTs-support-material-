package com.smart.manufacturing.strategy;

import com.smart.manufacturing.enums.Priority;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class LowPriorityStrategy implements PriorityStrategy {

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    @Override
    public int getPriorityWeight() {
        return 10;
    }

    @Override
    public BigDecimal calculateExpeditedSurcharge(BigDecimal baseSubtotal) {
        return BigDecimal.ZERO;
    }

    @Override
    public int calculateLeadTimeDays(int totalDurationHours) {
        // Run during idle machine windows (4 hours per day allocation)
        return Math.max(1, (int) Math.ceil((double) totalDurationHours / 4.0));
    }

    @Override
    public String getSchedulingPolicyDescription() {
        return "Low priority: scheduled during standard buffer windows and machine idle cycles.";
    }
}
