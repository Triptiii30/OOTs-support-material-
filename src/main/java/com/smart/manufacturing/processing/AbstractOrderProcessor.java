package com.smart.manufacturing.processing;

import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.exception.InvalidOrderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractOrderProcessor — Demonstrates abstract class (Abstraction).
 * Template Method pattern: defines algorithm skeleton; subclasses fill in steps.
 * Demonstrates: abstract class, abstract methods, protected, super, method overloading, StringBuilder.
 */
public abstract class AbstractOrderProcessor {

    protected static final Logger log = LoggerFactory.getLogger(AbstractOrderProcessor.class);
    protected final List<String> processingLog = new ArrayList<>();
    protected final String processorName;

    protected AbstractOrderProcessor(String processorName) {
        this.processorName = processorName;
    }

    // Template method — final, cannot be overridden
    public final ProcessingResult process(ManufacturingOrder order) {
        processingLog.add("[" + LocalDateTime.now() + "] Starting: " + processorName);
        try {
            validate(order);
            ProcessingResult result = execute(order);
            postProcess(order, result);
            processingLog.add("[" + LocalDateTime.now() + "] Completed: " + processorName);
            return result;
        } catch (Exception e) {
            processingLog.add("[" + LocalDateTime.now() + "] FAILED: " + e.getMessage());
            return ProcessingResult.failure(e.getMessage());
        }
    }

    // Abstract methods — subclasses MUST implement
    protected abstract void validate(ManufacturingOrder order);
    protected abstract ProcessingResult execute(ManufacturingOrder order);

    // Hook method — subclasses MAY override
    protected void postProcess(ManufacturingOrder order, ProcessingResult result) {
        log.debug("Post-processing order: {}", order.getOrderNumber());
    }

    // Method overloading — same name, different params
    public String getSummary() {
        return getSummary(false);
    }

    public String getSummary(boolean detailed) {
        StringBuilder sb = new StringBuilder(); // StringBuilder demonstration
        sb.append("Processor: ").append(processorName).append("\n");
        sb.append("Events: ").append(processingLog.size()).append("\n");
        if (detailed) {
            processingLog.forEach(entry -> sb.append("  ").append(entry).append("\n"));
        }
        return sb.toString();
    }

    public List<String> getProcessingLog() {
        return new ArrayList<>(processingLog); // return copy — Encapsulation
    }

    public String getProcessorName() { return processorName; }

    // Inner class — Composition (cannot exist without processor context)
    public static class ProcessingResult {
        private final boolean success;
        private final String message;
        private final LocalDateTime timestamp;

        private ProcessingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public static ProcessingResult success(String message) {
            return new ProcessingResult(true, message);
        }

        public static ProcessingResult failure(String message) {
            return new ProcessingResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
