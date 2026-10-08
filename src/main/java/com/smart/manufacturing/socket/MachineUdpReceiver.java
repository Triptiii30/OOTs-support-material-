package com.smart.manufacturing.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MachineUdpReceiver — Listens for UDP machine status datagrams.
 * Demonstrates: DatagramSocket receiver, byte-to-String conversion,
 * CopyOnWriteArrayList, daemon thread, try-with-resources.
 */
public class MachineUdpReceiver implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MachineUdpReceiver.class);

    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();
    private Thread receiverThread;

    public MachineUdpReceiver() { this(MachineUdpSender.DEFAULT_UDP_PORT); }
    public MachineUdpReceiver(int port) { this.port = port; }

    public void startAsync() {
        if (running.compareAndSet(false, true)) {
            receiverThread = new Thread(this, "MachineUdpReceiver");
            receiverThread.setDaemon(true);
            receiverThread.start();
            log.info("MachineUdpReceiver started on port {}", port);
        }
    }
    
    public void stop() {
        running.set(false);
        if (receiverThread != null) receiverThread.interrupt();
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            while (running.get()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                messages.add(message);
                log.debug("Received UDP packet: {}", message);
            }
        } catch (Exception e) {
            if (running.get()) {
                log.error("UDP Receiver error", e);
            }
        }
    }
    
    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public List<String> getReceivedMessages() {
        return new ArrayList<>(messages);
    }
    
    public void clearMessages() {
        messages.clear();
    }
}
