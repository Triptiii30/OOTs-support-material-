package com.smart.manufacturing.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ProductionSchedulerWorker — Worker thread processing orders from BlockingQueue.
 * Demonstrates: Runnable, BlockingQueue, AtomicInteger, thread coordination.
 */
public class ProductionSchedulerWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ProductionSchedulerWorker.class);

    private final BlockingQueue<Long> orderQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private volatile boolean active = true;
    private final String workerName;

    public ProductionSchedulerWorker(String workerName) {
        this.workerName = workerName;
    }

    public void submitOrder(Long orderId) {
        try { orderQueue.put(orderId); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Override
    public void run() {
        log.info("[{}] Worker started on: {}", workerName, Thread.currentThread().getName());
        while (active) {
            try {
                Long orderId = orderQueue.poll(5, TimeUnit.SECONDS);
                if (orderId != null) {
                    log.info("[{}] Processing order: {}", workerName, orderId);
                    processedCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("[{}] Worker stopped. Processed: {}", workerName, processedCount.get());
    }

    public void shutdown() { this.active = false; }
    public int getProcessedCount() { return processedCount.get(); }
    public String getWorkerName() { return workerName; }
    public int getQueueSize() { return orderQueue.size(); }
}
