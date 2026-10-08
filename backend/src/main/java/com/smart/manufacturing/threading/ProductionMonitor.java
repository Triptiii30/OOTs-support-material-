package com.smart.manufacturing.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ProductionMonitor — Daemon background thread for production monitoring.
 * Demonstrates: Thread, Runnable, daemon thread, thread priority,
 * thread lifecycle, AtomicBoolean, synchronization, Thread.sleep().
 */
@Component
public class ProductionMonitor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ProductionMonitor.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger checksPerformed = new AtomicInteger(0);
    private volatile int checkIntervalMs = 60000;
    private Thread monitorThread;

    public synchronized void start() {
        if (running.get()) { log.warn("ProductionMonitor already running"); return; }
        monitorThread = new Thread(this, "ProductionMonitor-Daemon");
        monitorThread.setDaemon(true);                          // Daemon thread
        monitorThread.setPriority(Thread.MIN_PRIORITY + 1);    // Thread priority
        running.set(true);
        monitorThread.start();
        log.info("ProductionMonitor started (thread ID: {})", monitorThread.getId());
    }

    public synchronized void stop() {
        running.set(false);
        if (monitorThread != null) monitorThread.interrupt();
        log.info("ProductionMonitor stopped");
    }

    @Override
    public void run() {
        log.info("ProductionMonitor running: {} | daemon={}",
                Thread.currentThread().getName(), Thread.currentThread().isDaemon());
        while (running.get()) {
            try {
                checksPerformed.incrementAndGet();
                log.debug("ProductionMonitor check #{}", checksPerformed.get());
                Thread.sleep(checkIntervalMs); // TIMED_WAITING state
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("ProductionMonitor interrupted — shutting down");
                break;
            }
        }
        log.info("ProductionMonitor terminated");
    }

    // Method overloading
    public MonitorStats getStats() { return getStats(false); }
    public MonitorStats getStats(boolean withThread) {
        String threadInfo = withThread && monitorThread != null
                ? monitorThread.getName() + " (ID:" + monitorThread.getId() + ")" : "N/A";
        return new MonitorStats(checksPerformed.get(), threadInfo, running.get());
    }

    public boolean isRunning() { return running.get(); }
    public void setCheckIntervalMs(int ms) { this.checkIntervalMs = ms; }

    public static class MonitorStats {
        private final int checksPerformed;
        private final String threadInfo;
        private final boolean active;

        public MonitorStats(int checksPerformed, String threadInfo, boolean active) {
            this.checksPerformed = checksPerformed;
            this.threadInfo = threadInfo;
            this.active = active;
        }

        public int getChecksPerformed() { return checksPerformed; }
        public String getThreadInfo() { return threadInfo; }
        public boolean isActive() { return active; }
    }
}
