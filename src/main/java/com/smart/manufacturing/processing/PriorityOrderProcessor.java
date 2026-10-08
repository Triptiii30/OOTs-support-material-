package com.smart.manufacturing.processing;

import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.enums.Priority;
import java.time.LocalDate;

public class PriorityOrderProcessor extends AbstractOrderProcessor {

    private final int urgencyThresholdDays;

    public PriorityOrderProcessor() {
        this(7); // this() — constructor delegation
    }

    public PriorityOrderProcessor(int urgencyThresholdDays) {
        super("PriorityOrderProcessor"); // super keyword
        this.urgencyThresholdDays = urgencyThresholdDays;
    }

    @Override
    protected void validate(ManufacturingOrder order) {
        if (order.getPriority() == null) throw new IllegalArgumentException("Priority cannot be null");
    }

    @Override
    protected ProcessingResult execute(ManufacturingOrder order) {
        if (order.getRequiredDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), order.getRequiredDate());
            if (days <= urgencyThresholdDays && order.getPriority() != Priority.URGENT) {
                order.setPriority(Priority.HIGH);
                processingLog.add("Priority upgraded to HIGH: " + order.getOrderNumber() + " (" + days + " days)");
            }
        }
        return ProcessingResult.success("Priority: " + order.getPriority());
    }
}
