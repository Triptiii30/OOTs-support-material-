package com.smart.manufacturing.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * ProductionTcpClient — TCP client for production floor terminals.
 * Demonstrates: Socket, BufferedReader, PrintWriter, try-with-resources.
 */
public class ProductionTcpClient {

    private static final Logger log = LoggerFactory.getLogger(ProductionTcpClient.class);

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;

    public ProductionTcpClient() { this("localhost", ProductionTcpServer.DEFAULT_PORT); }
    public ProductionTcpClient(String host, int port) { this.host = host; this.port = port; }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            String welcome = in.readLine();
            connected = true;
            log.info("Connected: {}", welcome);
            return true;
        } catch (IOException e) {
            log.warn("Cannot connect to {}:{} — {}", host, port, e.getMessage());
            return false;
        }
    }

    public String sendCommand(String command) {
        if (!connected) return "ERROR: Not connected";
        try { out.println(command); return in.readLine(); }
        catch (IOException e) { connected = false; return "ERROR: " + e.getMessage(); }
    }

    // Method overloading
    public String ping() { return sendCommand("PING"); }
    public String startTask(Long taskId) { return sendCommand("START_TASK " + taskId); }
    public String pauseTask(Long taskId) { return sendCommand("PAUSE_TASK " + taskId); }
    public String completeTask(Long taskId) { return sendCommand("COMPLETE_TASK " + taskId); }
    public String getStatus() { return sendCommand("GET_STATUS"); }

    public void disconnect() { sendCommand("DISCONNECT"); close(); }

    public void close() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() { return connected; }
    public String getHost() { return host; }
    public int getPort() { return port; }
}
