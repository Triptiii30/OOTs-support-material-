package com.smart.manufacturing.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ProductionTcpServer — TCP server for production floor terminal communication.
 * Demonstrates: ServerSocket, Socket, BufferedReader, PrintWriter, try-with-resources,
 * multi-threaded server, inner class, thread pool.
 * Protocol: PING, START_TASK, PAUSE_TASK, COMPLETE_TASK, GET_STATUS, DISCONNECT
 * Run via: new ProductionTcpServer().startAsync()
 */
public class ProductionTcpServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ProductionTcpServer.class);
    public static final int DEFAULT_PORT = 9090;

    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService pool = Executors.newFixedThreadPool(5);
    private ServerSocket serverSocket;

    public ProductionTcpServer() { this(DEFAULT_PORT); } // method overloading
    public ProductionTcpServer(int port) { this.port = port; }

    @Override
    public void run() {
        running.set(true);
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            ss.setSoTimeout(5000);
            log.info("ProductionTcpServer listening on port {}", port);
            while (running.get()) {
                try {
                    Socket client = ss.accept();
                    log.info("TCP client connected: {}", client.getInetAddress());
                    pool.execute(new ClientHandler(client));
                } catch (SocketTimeoutException ignored) {}
            }
        } catch (IOException e) {
            if (running.get()) log.error("TcpServer error: {}", e.getMessage());
        } finally {
            pool.shutdown();
            log.info("ProductionTcpServer stopped");
        }
    }

    public void startAsync() {
        Thread t = new Thread(this, "ProductionTcpServer");
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        running.set(false);
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    public boolean isRunning() { return running.get(); }
    public int getPort() { return port; }

    // Inner class — handles one client connection
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            // try-with-resources — auto-close character streams
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
                out.println("CONNECTED Manufacturing Production Server v1.0");
                String cmd;
                while ((cmd = in.readLine()) != null) {
                    String resp = handle(cmd.trim().toUpperCase());
                    out.println(resp);
                    if ("DISCONNECT".equals(cmd.trim().toUpperCase())) break;
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(ClientHandler.class).debug("Client disconnected: {}", e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private String handle(String cmd) {
            if ("PING".equals(cmd)) return "PONG";
            if (cmd.startsWith("START_TASK")) return "ACK START_TASK";
            if (cmd.startsWith("PAUSE_TASK")) return "ACK PAUSE_TASK";
            if (cmd.startsWith("COMPLETE_TASK")) return "ACK COMPLETE_TASK";
            if ("GET_STATUS".equals(cmd)) return "STATUS ACTIVE";
            if ("DISCONNECT".equals(cmd)) return "GOODBYE";
            return "ERROR Unknown: " + cmd;
        }
    }
}
