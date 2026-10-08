package com.smart.manufacturing.desktop;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ProductionTerminal — Swing/AWT desktop application for production floor operators.
 *
 * Demonstrates:
 * - javax.swing (JFrame, JPanel, JButton, JTextArea, JLabel, JTextField, JScrollPane)
 * - java.awt (Color, Font, FlowLayout, BorderLayout, GridBagLayout)
 * - Event Handling (ActionListener, WindowAdapter, KeyListener)
 * - Layout Managers (BorderLayout, GridBagLayout, FlowLayout)
 * - TCP Socket communication with ProductionTcpServer
 * - Inner classes (ConnectionPanel, ControlPanel, LogPanel)
 * - Thread safety for Swing (SwingUtilities.invokeLater)
 *
 * Run: java -jar production-terminal.jar
 * Or:  mvn exec:java (from desktop-client directory)
 *
 * Default server: localhost:9090
 */
public class ProductionTerminal extends JFrame {

    // Swing Components
    private JTextField serverHostField;
    private JTextField serverPortField;
    private JTextField taskIdField;
    private JTextArea logArea;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton pingButton;
    private JButton startTaskButton;
    private JButton pauseTaskButton;
    private JButton completeTaskButton;
    private JButton getStatusButton;
    private JLabel connectionStatusLabel;
    private JLabel lastResponseLabel;

    // Connection state
    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private volatile boolean connected = false;

    // Constants
    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color ACCENT_GREEN = new Color(0, 200, 100);
    private static final Color ACCENT_ORANGE = new Color(255, 140, 0);
    private static final Color ACCENT_RED = new Color(220, 50, 50);
    private static final Color PANEL_BG = new Color(45, 45, 45);
    private static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);

    public ProductionTerminal() {
        super("Smart Manufacturing — Production Floor Terminal");
        initializeUI();
        setupWindowBehavior();
        log("Production Terminal initialized. Connect to server to begin.");
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(DARK_BG);

        // Main layout — BorderLayout
        setLayout(new BorderLayout(8, 8));

        // Top: Title bar
        add(createTitlePanel(), BorderLayout.NORTH);
        // Left: Connection + Controls
        add(createControlPanel(), BorderLayout.WEST);
        // Center: Log area
        add(createLogPanel(), BorderLayout.CENTER);
        // Bottom: Status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    // ===== PANEL BUILDERS =====

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // FlowLayout
        panel.setBackground(new Color(20, 60, 100));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("\u2699 Smart Manufacturing — Production Floor Terminal");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        panel.add(title);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(DARK_BG);
        outer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        outer.setPreferredSize(new Dimension(260, 0));

        outer.add(createConnectionPanel());
        outer.add(Box.createVerticalStrut(8));
        outer.add(createTaskPanel());
        outer.add(Box.createVerticalStrut(8));
        outer.add(createCommandPanel());
        outer.add(Box.createVerticalGlue());

        return outer;
    }

    private JPanel createConnectionPanel() {
        JPanel panel = styledPanel("Server Connection");
        panel.setLayout(new GridBagLayout()); // GridBagLayout demonstration
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        // Host field
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        panel.add(label("Host:"), gbc);
        serverHostField = textField("localhost");
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(serverHostField, gbc);

        // Port field
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(label("Port:"), gbc);
        serverPortField = textField("9090");
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(serverPortField, gbc);

        // Connect/Disconnect buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        connectButton = actionButton("Connect", ACCENT_GREEN, e -> handleConnect());
        panel.add(connectButton, gbc);

        gbc.gridy = 3;
        disconnectButton = actionButton("Disconnect", ACCENT_RED, e -> handleDisconnect());
        disconnectButton.setEnabled(false);
        panel.add(disconnectButton, gbc);

        return panel;
    }

    private JPanel createTaskPanel() {
        JPanel panel = styledPanel("Task Control");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(label("Task ID:"), gbc);
        taskIdField = textField("1");
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(taskIdField, gbc);

        String[] taskButtons = {"Start Task", "Pause Task", "Complete Task"};
        ActionListener[] taskListeners = {
            e -> sendTaskCommand("START_TASK"),
            e -> sendTaskCommand("PAUSE_TASK"),
            e -> sendTaskCommand("COMPLETE_TASK")
        };
        Color[] colors = {ACCENT_GREEN, ACCENT_ORANGE, new Color(100, 100, 200)};

        for (int i = 0; i < taskButtons.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.gridwidth = 2; gbc.weightx = 1;
            JButton btn = actionButton(taskButtons[i], colors[i], taskListeners[i]);
            btn.setEnabled(false);
            switch (i) {
                case 0 -> startTaskButton = btn;
                case 1 -> pauseTaskButton = btn;
                case 2 -> completeTaskButton = btn;
            }
            panel.add(btn, gbc);
        }
        return panel;
    }

    private JPanel createCommandPanel() {
        JPanel panel = styledPanel("Server Commands");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.gridwidth = 2; gbc.weightx = 1;

        pingButton = actionButton("PING Server", new Color(60, 120, 200), e -> sendCommand("PING"));
        pingButton.setEnabled(false);
        gbc.gridy = 0; panel.add(pingButton, gbc);

        getStatusButton = actionButton("GET STATUS", new Color(120, 80, 160), e -> sendCommand("GET_STATUS"));
        getStatusButton.setEnabled(false);
        gbc.gridy = 1; panel.add(getStatusButton, gbc);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = styledPanel("Communication Log");
        panel.setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setBackground(new Color(10, 10, 10));
        logArea.setForeground(ACCENT_GREEN);
        logArea.setFont(MONO_FONT);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(6, 6, 6, 6));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBackground(DARK_BG);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Clear log button
        JButton clearButton = new JButton("Clear Log");
        clearButton.setForeground(Color.LIGHT_GRAY);
        clearButton.setBackground(new Color(60, 60, 60));
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> logArea.setText(""));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(PANEL_BG);
        btnPanel.add(clearButton);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        connectionStatusLabel = new JLabel("\u25CF DISCONNECTED");
        connectionStatusLabel.setForeground(ACCENT_RED);
        connectionStatusLabel.setFont(TITLE_FONT);

        lastResponseLabel = new JLabel("No response yet");
        lastResponseLabel.setForeground(Color.GRAY);
        lastResponseLabel.setFont(MONO_FONT);

        panel.add(connectionStatusLabel, BorderLayout.WEST);
        panel.add(lastResponseLabel, BorderLayout.EAST);
        return panel;
    }

    // ===== EVENT HANDLERS =====

    private void handleConnect() {
        String host = serverHostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(serverPortField.getText().trim());
        } catch (NumberFormatException e) {
            log("ERROR: Invalid port number");
            return;
        }

        // Connect in background thread — keep UI responsive
        new Thread(() -> {
            log("Connecting to " + host + ":" + port + "...");
            try {
                socket = new Socket(host, port);
                serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                serverOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                String welcome = serverIn.readLine();
                connected = true;

                // Update UI on Event Dispatch Thread (Swing thread safety)
                SwingUtilities.invokeLater(() -> {
                    setConnectedState(true);
                    log("CONNECTED: " + welcome);
                });

                // Start listening for server responses
                listenForResponses();

            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    log("CONNECTION FAILED: " + e.getMessage());
                    setConnectedState(false);
                });
            }
        }, "ConnectionThread").start();
    }

    private void handleDisconnect() {
        sendCommand("DISCONNECT");
        closeConnection();
    }

    private void sendTaskCommand(String command) {
        String taskId = taskIdField.getText().trim();
        sendCommand(command + " " + taskId);
    }

    private void sendCommand(String command) {
        if (!connected || serverOut == null) {
            log("ERROR: Not connected to server");
            return;
        }
        log(">>> " + command);
        serverOut.println(command);
    }

    private void listenForResponses() {
        new Thread(() -> {
            try {
                String response;
                while (connected && (response = serverIn.readLine()) != null) {
                    final String r = response;
                    SwingUtilities.invokeLater(() -> {
                        log("<<< " + r);
                        lastResponseLabel.setText(r.length() > 60 ? r.substring(0, 57) + "..." : r);
                    });
                    if (r.equals("GOODBYE")) break;
                }
            } catch (IOException e) {
                if (connected) {
                    SwingUtilities.invokeLater(() -> {
                        log("Connection lost: " + e.getMessage());
                        setConnectedState(false);
                    });
                }
            }
        }, "ResponseListenerThread").start();
    }

    // ===== HELPER METHODS =====

    private void setConnectedState(boolean connected) {
        this.connected = connected;
        connectButton.setEnabled(!connected);
        disconnectButton.setEnabled(connected);
        pingButton.setEnabled(connected);
        getStatusButton.setEnabled(connected);
        startTaskButton.setEnabled(connected);
        pauseTaskButton.setEnabled(connected);
        completeTaskButton.setEnabled(connected);

        if (connected) {
            connectionStatusLabel.setText("\u25CF CONNECTED");
            connectionStatusLabel.setForeground(ACCENT_GREEN);
        } else {
            connectionStatusLabel.setText("\u25CF DISCONNECTED");
            connectionStatusLabel.setForeground(ACCENT_RED);
        }
    }

    private void closeConnection() {
        connected = false;
        try {
            if (serverIn != null) serverIn.close();
            if (serverOut != null) serverOut.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        SwingUtilities.invokeLater(() -> {
            setConnectedState(false);
            log("Disconnected from server");
        });
    }

    private void log(String message) {
        // Must update Swing components on Event Dispatch Thread
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String entry = "[" + timestamp + "] " + message + "\n";
        SwingUtilities.invokeLater(() -> {
            logArea.append(entry);
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
        });
    }

    private void setupWindowBehavior() {
        // WindowAdapter — Event Handling for window events
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (connected) closeConnection();
            }
        });
    }

    // ===== UI FACTORY METHODS (Demonstrates method overloading) =====

    private JPanel styledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)), title);
        border.setTitleColor(Color.LIGHT_GRAY);
        border.setTitleFont(TITLE_FONT);
        panel.setBorder(border);
        return panel;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.LIGHT_GRAY);
        lbl.setFont(MONO_FONT);
        return lbl;
    }

    private JTextField textField(String defaultValue) {
        JTextField tf = new JTextField(defaultValue, 10);
        tf.setBackground(new Color(55, 55, 55));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(MONO_FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 90)),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        return tf;
    }

    private JButton actionButton(String text, Color accentColor, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setBackground(accentColor.darker().darker());
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor.darker()),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener); // Event handling — lambda ActionListener

        // Hover effect (MouseAdapter — Event Handling)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(accentColor.darker());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(accentColor.darker().darker());
            }
        });

        return btn;
    }

    // ===== ENTRY POINT =====

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ProductionTerminal terminal = new ProductionTerminal();
            terminal.setVisible(true);
        });
    }
}
