package com.smart.manufacturing.processing;

import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.exception.InvalidOrderException;

public class ValidationOrderProcessor extends AbstractOrderProcessor {

    public ValidationOrderProcessor() {
        super("ValidationOrderProcessor");
    }

    @Override
    protected void validate(ManufacturingOrder order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new InvalidOrderException("Order must have at least one item");
        }
    }

    @Override
    protected ProcessingResult execute(ManufacturingOrder order) {
        if (order.getCustomer() == null) return ProcessingResult.failure("No customer assigned");
        if (order.getRequiredDate() == null) return ProcessingResult.failure("No delivery date set");
        processingLog.add("Validated: " + order.getOrderNumber());
        return ProcessingResult.success("Order " + order.getOrderNumber() + " is valid");
    }

    @Override
    protected void postProcess(ManufacturingOrder order, ProcessingResult result) {
        super.postProcess(order, result); // super keyword
        if (result.isSuccess()) log.info("Validation complete: {}", order.getOrderNumber());
    }
}
