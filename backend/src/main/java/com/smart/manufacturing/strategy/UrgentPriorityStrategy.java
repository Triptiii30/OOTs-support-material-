package com.smart.manufacturing.strategy;

import com.smart.manufacturing.enums.Priority;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class UrgentPriorityStrategy implements PriorityStrategy {

    @Override
    public Priority getPriority() {
        return Priority.URGENT;
    }

    @Override
    public int getPriorityWeight() {
        return 1000;
    }

    @Override
    public BigDecimal calculateExpeditedSurcharge(BigDecimal baseSubtotal) {
        // 15% urgent emergency queue preemption surcharge
        if (baseSubtotal == null) return BigDecimal.ZERO;
        return baseSubtotal.multiply(BigDecimal.valueOf(0.15)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int calculateLeadTimeDays(int totalDurationHours) {
        // Continuous 24/7 dedicated production shift (24 hours per day allocation)
        return Math.max(1, (int) Math.ceil((double) totalDurationHours / 24.0));
    }

    @Override
    public String getSchedulingPolicyDescription() {
        return "Urgent priority: continuous 24/7 machine preemption with maximum queue precedence.";
    }
}
