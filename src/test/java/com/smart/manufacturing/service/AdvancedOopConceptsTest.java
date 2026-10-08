package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.GenericResponse;
import com.smart.manufacturing.dto.PagedResponse;
import com.smart.manufacturing.entity.*;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.processing.*;
import com.smart.manufacturing.socket.MachineUdpReceiver;
import com.smart.manufacturing.socket.MachineUdpSender;
import com.smart.manufacturing.socket.ProductionTcpClient;
import com.smart.manufacturing.socket.ProductionTcpServer;
import com.smart.manufacturing.strategy.*;
import com.smart.manufacturing.threading.ProductionMonitor;
import com.smart.manufacturing.threading.ProductionSchedulerWorker;
import com.smart.manufacturing.util.ArrayDemoService;
import com.smart.manufacturing.util.StringProcessingUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Advanced OOP & Unit 1-5 Concepts Test Suite")
class AdvancedOopConceptsTest {

    @Test
    @DisplayName("User Inheritance & Polymorphism Test")
    void testUserInheritanceHierarchy() {
        User admin = new AdminUser("admin1", "admin1@factory.com", "pass");
        User manager = new ProductionManagerUser("mgr1", "mgr1@factory.com", "pass");
        User staff = new OrderStaffUser("staff1", "staff1@factory.com", "pass");
        User coord = new InventoryCoordinatorUser("coord1", "coord1@factory.com", "pass");

        // Subtype polymorphism - dynamic method dispatch
        assertTrue(admin.getDisplayRole().contains("Administrator"));
        assertTrue(manager.getDisplayRole().contains("Production Manager"));
        assertTrue(staff.getDisplayRole().contains("Order Processing"));
        assertTrue(coord.getDisplayRole().contains("Inventory Coordinator"));

        assertTrue(admin.hasPermission("ANY_ACTION"));
        assertTrue(manager.hasPermission("PRODUCTION_START"));
        assertFalse(manager.hasPermission("DELETE_USER"));
    }

    @Test
    @DisplayName("Abstract Order Processor Template Method Test")
    void testAbstractOrderProcessor() {
        ManufacturingOrder order = new ManufacturingOrder();
        order.setOrderNumber("ORD-TEST-999");
        order.setPriority(Priority.NORMAL);
        order.setRequiredDeliveryDate(LocalDate.now().plusDays(3)); // Urgent proximity

        ValidationOrderProcessor valProcessor = new ValidationOrderProcessor();
        PriorityOrderProcessor priorityProcessor = new PriorityOrderProcessor(5);

        // Subclass overrides template method steps
        AbstractOrderProcessor.ProcessingResult valResult = valProcessor.process(order);
        // Will fail validation because order has no customer and no items yet
        assertFalse(valResult.isSuccess());

        // Now process with PriorityOrderProcessor
        AbstractOrderProcessor.ProcessingResult priorityResult = priorityProcessor.process(order);
        assertTrue(priorityResult.isSuccess());
        assertEquals(Priority.HIGH, order.getPriority()); // Auto-upgraded due to <= 5 days proximity!

        // Overloaded getSummary() tests
        assertNotNull(priorityProcessor.getSummary());
        assertNotNull(priorityProcessor.getSummary(true));
    }

    @Test
    @DisplayName("Polymorphic Scheduling Strategy Test (PriorityQueue)")
    void testSchedulingStrategies() {
        ManufacturingOrder o1 = new ManufacturingOrder();
        o1.setOrderNumber("ORD-1");
        o1.setPriority(Priority.LOW);
        o1.setRequiredDeliveryDate(LocalDate.now().plusDays(2));

        ManufacturingOrder o2 = new ManufacturingOrder();
        o2.setOrderNumber("ORD-2");
        o2.setPriority(Priority.URGENT);
        o2.setRequiredDeliveryDate(LocalDate.now().plusDays(10));

        List<ManufacturingOrder> orders = List.of(o1, o2);

        // Priority Strategy: URGENT first regardless of deadline
        SchedulingStrategy priorityStrategy = new PrioritySchedulingStrategy();
        PriorityQueue<ManufacturingOrder> pq1 = priorityStrategy.schedule(orders);
        assertEquals("ORD-2", pq1.poll().getOrderNumber());

        // Deadline Strategy: Earliest deadline first (ORD-1: 2 days)
        SchedulingStrategy deadlineStrategy = new DeadlineSchedulingStrategy();
        PriorityQueue<ManufacturingOrder> pq2 = deadlineStrategy.schedule(orders);
        assertEquals("ORD-1", pq2.poll().getOrderNumber());

        // Balanced Strategy
        SchedulingStrategy balancedStrategy = new BalancedSchedulingStrategy();
        PriorityQueue<ManufacturingOrder> pq3 = balancedStrategy.schedule(orders);
        assertFalse(pq3.isEmpty());
    }

    @Test
    @DisplayName("Generics and Bounded Types Test")
    void testGenerics() {
        GenericResponse<String> textResp = GenericResponse.success("Data Saved", "OK");
        assertTrue(textResp.isSuccess());
        assertEquals("Data Saved", textResp.getData());

        // Bounded Generic method: <N extends Number>
        GenericResponse<Double> numResp = GenericResponse.numericSuccess(1250.50, "Total");
        assertTrue(numResp.isSuccess());
        assertEquals(1250.50, numResp.getData());

        // PagedResponse with lower-bounded wildcard
        List<Integer> values = List.of(10, 20, 30, 40);
        double sum = PagedResponse.sumValues(values);
        assertEquals(100.0, sum);
    }

    @Test
    @DisplayName("Array Service (1D, 2D, Jagged Arrays) Test")
    void testArrayService() {
        ArrayDemoService arrayService = new ArrayDemoService();

        // 1D Array
        int[] targets = arrayService.getMonthlyProductionTargets();
        assertEquals(12, targets.length);
        assertTrue(arrayService.computeAnnualTarget() > 0);

        // 2D Array
        int[][] matrix = arrayService.getWorkstationCapacityMatrix();
        assertEquals(4, matrix.length);
        assertEquals(3, matrix[0].length);
        assertTrue(arrayService.getPeakShiftForWorkstation(0) >= 0);

        // Jagged Array (varying row lengths)
        int[][] jagged = arrayService.getProductionLineStageCapacities();
        assertEquals(3, jagged.length);
        assertEquals(5, jagged[0].length); // Line 0: 5 stages
        assertEquals(3, jagged[1].length); // Line 1: 3 stages
        assertEquals(2, jagged[2].length); // Line 2: 2 stages
    }

    @Test
    @DisplayName("String Utilities (StringBuilder, StringBuffer, StringTokenizer) Test")
    void testStringUtilities() {
        // StringBuilder
        String summary = StringProcessingUtil.buildOrderSummary("ORD-101", "Acme", "IN_PROGRESS", "HIGH", 50000.0);
        assertTrue(summary.contains("ORD-101"));
        assertTrue(summary.contains("Acme"));

        // StringBuffer (thread-safe log)
        StringBuffer logBuf = new StringBuffer();
        StringProcessingUtil.appendToLog(logBuf, "Machine calibration complete");
        assertTrue(logBuf.toString().contains("Machine calibration"));

        // StringTokenizer
        List<String> tokens = StringProcessingUtil.tokenizeCsvLine("ORD-202,Product-X,15,999.00");
        assertEquals(4, tokens.size());
        assertEquals("ORD-202", tokens.get(0));

        // String immutability & validation
        assertEquals("ORD-500", StringProcessingUtil.processOrderId("  ord-500  "));
        assertTrue(StringProcessingUtil.isValidOrderNumber("ORD-2026-001"));
        assertFalse(StringProcessingUtil.isValidOrderNumber("INVALID"));
    }

    @Test
    @DisplayName("Multithreading: ProductionMonitor and Worker Queue Test")
    void testMultithreadingComponents() throws InterruptedException {
        // Daemon Production Monitor
        ProductionMonitor monitor = new ProductionMonitor();
        monitor.setCheckIntervalMs(50);
        monitor.start();
        assertTrue(monitor.isRunning());
        Thread.sleep(120);
        ProductionMonitor.MonitorStats stats = monitor.getStats(true);
        assertTrue(stats.getChecksPerformed() >= 1);
        monitor.stop();
        assertFalse(monitor.isRunning());

        // Scheduler Worker Thread with BlockingQueue
        ProductionSchedulerWorker worker = new ProductionSchedulerWorker("Worker-1");
        Thread workerThread = new Thread(worker, "TestWorkerThread");
        workerThread.start();
        worker.submitOrder(101L);
        worker.submitOrder(102L);
        Thread.sleep(100);
        worker.shutdown();
        workerThread.join(500);
        assertEquals(2, worker.getProcessedCount());
    }

    @Test
    @DisplayName("Networking: TCP Server-Client and UDP Telemetry Test")
    void testSocketCommunication() throws Exception {
        int tcpPort = 9876;
        int udpPort = 9877;

        // 1. TCP Server and Client
        ProductionTcpServer server = new ProductionTcpServer(tcpPort);
        server.startAsync();
        Thread.sleep(100); // Give server time to bind

        ProductionTcpClient client = new ProductionTcpClient("localhost", tcpPort);
        assertTrue(client.connect());
        assertEquals("PONG", client.ping());
        assertTrue(client.startTask(55L).contains("ACK START_TASK"));
        assertTrue(client.getStatus().contains("STATUS ACTIVE"));
        client.disconnect();
        server.stop();

        // 2. UDP Sender and Receiver
        MachineUdpReceiver receiver = new MachineUdpReceiver(udpPort);
        receiver.startAsync();
        Thread.sleep(50);

        MachineUdpSender sender = new MachineUdpSender("localhost", udpPort, "LATHE-01");
        sender.sendHeartbeat();
        sender.sendProductionUpdate(85);
        Thread.sleep(100);

        List<String> messages = receiver.getReceivedMessages();
        assertFalse(messages.isEmpty());
        assertTrue(messages.stream().anyMatch(m -> m.contains("LATHE-01|HEARTBEAT")));
        receiver.stop();
    }
}
