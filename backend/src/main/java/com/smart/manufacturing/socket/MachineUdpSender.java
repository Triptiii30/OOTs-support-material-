package com.smart.manufacturing.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * MachineUdpSender — Sends UDP status datagrams (fire-and-forget).
 * Demonstrates: DatagramSocket, DatagramPacket, byte arrays, try-with-resources, UDP.
 */
public class MachineUdpSender {

    private static final Logger log = LoggerFactory.getLogger(MachineUdpSender.class);
    public static final int DEFAULT_UDP_PORT = 9091;

    private final String host;
    private final int port;
    private final String machineId;

    public MachineUdpSender(String machineId) { this("localhost", DEFAULT_UDP_PORT, machineId); }
    public MachineUdpSender(String host, int port, String machineId) {
        this.host = host; this.port = port; this.machineId = machineId;
    }

    public boolean sendStatus(String status, String value) {
        String msg = machineId + "|" + status + "|" + value;
        byte[] data = msg.getBytes(StandardCharsets.UTF_8); // byte-oriented
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket pkt = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            socket.send(pkt);
            log.debug("UDP sent [{}]: {}", machineId, msg);
            return true;
        } catch (Exception e) {
            log.warn("UDP send failed [{}]: {}", machineId, e.getMessage());
            return false;
        }
    }

    // Method overloading
    public boolean sendHeartbeat() { return sendStatus("HEARTBEAT", "OK"); }
    public boolean sendProductionUpdate(int progress) { return sendStatus("PROGRESS", String.valueOf(progress)); }
    public boolean sendAlarm(String code) { return sendStatus("ALARM", code); }

    public String getMachineId() { return machineId; }
}
