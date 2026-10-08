package com.smart.manufacturing.strategy;

import com.smart.manufacturing.enums.Priority;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HighPriorityStrategy implements PriorityStrategy {

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @Override
    public int getPriorityWeight() {
        return 500;
    }

    @Override
    public BigDecimal calculateExpeditedSurcharge(BigDecimal baseSubtotal) {
        // 5% expedited allocation surcharge
        if (baseSubtotal == null) return BigDecimal.ZERO;
        return baseSubtotal.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int calculateLeadTimeDays(int totalDurationHours) {
        // Dual-shift production line (16 hours per day allocation)
        return Math.max(1, (int) Math.ceil((double) totalDurationHours / 16.0));
    }

    @Override
    public String getSchedulingPolicyDescription() {
        return "High priority: dual-shift fast-track queue with priority tool allocation.";
    }
}
