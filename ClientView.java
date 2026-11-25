package com.mycompany.campuslostandfound.gui;

import com.mycompany.campuslostandfound.client.ChatClient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientView extends JFrame {
    private final Color NAVY = new Color(18, 38, 70);
    private final Color BLUE = new Color(45, 105, 220);
    private final Color LIGHT = new Color(244, 247, 252);
    private final Color GREEN = new Color(35, 160, 105);

    private final JTextField txtNickname = new JTextField("Student", 10);
    private final JTextField txtServerIP = new JTextField("127.0.0.1", 12);
    private final JTextField txtPort = new JTextField("5000", 5);
    private final JButton btnConnect = new JButton("Connect");
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextArea txtChat = new JTextArea();
    private final JTextField txtMessage = new JTextField();
    private final DefaultListModel<String> usersModel = new DefaultListModel<>();
    private DefaultTableModel itemModel;
    private JTable tblItems;
    private ChatClient client;
    private JLabel connectionStatus;

    public ClientView() {
        setTitle("Campus Lost & Found");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(LIGHT);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(LIGHT);
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainContent(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> connect());
        btnDisconnect.addActionListener(e -> disconnect());
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 12));
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        JLabel title = new JLabel("CAMPUS LOST & FOUND");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel subtitle = new JLabel("Community item reporting and campus chat");
        subtitle.setForeground(new Color(205, 218, 238));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        brand.add(title, BorderLayout.NORTH);
        brand.add(subtitle, BorderLayout.SOUTH);
        header.add(brand, BorderLayout.WEST);

        JPanel connection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 4));
        connection.setOpaque(false);
        styleField(txtNickname);
        styleField(txtServerIP);
        styleField(txtPort);
        connection.add(labelledField("Nickname", txtNickname));
        connection.add(labelledField("Server IP", txtServerIP));
        connection.add(labelledField("Port", txtPort));
        styleButton(btnConnect, BLUE);
        styleButton(btnDisconnect, new Color(90, 105, 125));
        connection.add(btnConnect);
        connection.add(btnDisconnect);
        header.add(connection, BorderLayout.EAST);
        return header;
    }

    private JPanel labelledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(3, 3));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JComponent createMainContent() {
        JPanel main = new JPanel(new BorderLayout(14, 14));
        main.setBackground(LIGHT);
        main.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Lost & Found Board", createItemsPanel());
        tabs.addTab("Campus Group Chat", createChatPanel());
        main.add(tabs, BorderLayout.CENTER);

        JPanel users = createUsersPanel();
        users.setPreferredSize(new Dimension(190, 0));
        main.add(users, BorderLayout.EAST);
        return main;
    }

    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel heading = new JLabel("Lost and Found Items");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(NAVY);
        JLabel hint = new JLabel("Report lost belongings or items you found around campus.");
        hint.setForeground(new Color(100, 110, 125));
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 3));
        labels.setOpaque(false);
        labels.add(heading);
        labels.add(hint);
        top.add(labels, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        JButton lost = new JButton("+ Report Lost");
        JButton found = new JButton("+ Report Found");
        JButton claim = new JButton("Request Claim");
        styleButton(lost, new Color(220, 70, 70));
        styleButton(found, GREEN);
        styleButton(claim, BLUE);
        actions.add(lost); actions.add(found); actions.add(claim);
        top.add(actions, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        itemModel = new DefaultTableModel(new Object[]{"Type", "Item Name", "Category", "Description", "Location", "Date", "Reporter"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblItems = new JTable(itemModel);
        tblItems.setRowHeight(30);
        tblItems.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tblItems.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tblItems.getTableHeader().setBackground(NAVY);
        tblItems.getTableHeader().setForeground(Color.WHITE);
        tblItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblItems.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scroll = new JScrollPane(tblItems);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        lost.addActionListener(e -> reportItem("LOST"));
        found.addActionListener(e -> reportItem("FOUND"));
        claim.addActionListener(e -> requestClaim());
        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel heading = new JLabel("Campus Group Chat");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(NAVY);
        panel.add(heading, BorderLayout.NORTH);

        txtChat.setEditable(false);
        txtChat.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);
        txtChat.setBackground(new Color(249, 251, 254));
        txtChat.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(txtChat), BorderLayout.CENTER);

        JPanel send = new JPanel(new BorderLayout(8, 8));
        send.setOpaque(false);
        txtMessage.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JButton sendButton = new JButton("Send");
        styleButton(sendButton, BLUE);
        send.add(txtMessage, BorderLayout.CENTER);
        send.add(sendButton, BorderLayout.EAST);
        panel.add(send, BorderLayout.SOUTH);
        sendButton.addActionListener(e -> sendChat());
        txtMessage.addActionListener(e -> sendChat());
        return panel;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel title = new JLabel("ONLINE USERS");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(NAVY);
        panel.add(title, BorderLayout.NORTH);

        JList<String> users = new JList<>(usersModel);
        users.setFont(new Font("SansSerif", Font.PLAIN, 13));
        users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(users), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(232, 237, 245));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        connectionStatus = new JLabel("● Disconnected");
        connectionStatus.setForeground(new Color(180, 55, 55));
        connectionStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel note = new JLabel("Campus Lost & Found • Local Network Application");
        note.setForeground(new Color(90, 100, 115));
        footer.add(connectionStatus, BorderLayout.WEST);
        footer.add(note, BorderLayout.EAST);
        return footer;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 198, 218)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setOpaque(true);
    }

    private void connect() {
        if (client != null && client.isConnected()) return;
        String nickname = txtNickname.getText().trim();
        String ip = txtServerIP.getText().trim();
        if (nickname.isBlank() || ip.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter a nickname and server IP.");
            return;
        }
        int port;
        try { port = Integer.parseInt(txtPort.getText().trim()); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be a number.");
            return;
        }
        client = new ChatClient(ip, port, nickname, this);
        client.connect();
    }

    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            connectionStatus.setText("● Connected");
            connectionStatus.setForeground(GREEN);
            appendChat("[System] Connected to server.");
        });
    }

    public void onServerMessage(String line) {
        SwingUtilities.invokeLater(() -> {
            if (line.startsWith("CHAT|")) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) appendChat("[" + timeNow() + "] " + parts[1] + ": " + parts[2]);
            } else if (line.startsWith("USERLIST|")) {
                usersModel.clear();
                String users = line.substring(9);
                if (!users.isBlank()) for (String user : users.split(",")) usersModel.addElement(user);
            } else if (line.startsWith("ITEM|")) {
                addItemRow(line.substring(5));
            } else if (line.startsWith("INFO|")) {
                appendChat("[System] " + line.substring(5));
            } else if (line.startsWith("ERROR|")) {
                JOptionPane.showMessageDialog(this, line.substring(6), "Server message", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    public void onConnectionError(String message) {
        SwingUtilities.invokeLater(() -> {
            connectionStatus.setText("● Disconnected");
            connectionStatus.setForeground(new Color(180, 55, 55));
            JOptionPane.showMessageDialog(this, message, "Connection error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void sendChat() {
        String message = txtMessage.getText().trim();
        if (message.isBlank()) return;
        if (client == null || !client.isConnected()) {
            appendChat("[System] Connect to the server first.");
            return;
        }
        if (message.equals("/help")) {
            appendChat("[System] Commands: /help, /name <newname>, /exit");
        } else if (message.startsWith("/name ")) {
            client.send("RENAME|" + message.substring(6).trim());
        } else if (message.equals("/exit")) {
            disconnect();
        } else {
            client.send("CHAT|" + message);
        }
        txtMessage.setText("");
    }

    private void reportItem(String type) {
        if (client == null || !client.isConnected()) {
            JOptionPane.showMessageDialog(this, "Connect to the server first.");
            return;
        }
        JTextField name = new JTextField();
        JTextField category = new JTextField();
        JTextField description = new JTextField();
        JTextField location = new JTextField();
        Object[] fields = {"Item name:", name, "Category:", category, "Description:", description, "Location:", location};
        int result = JOptionPane.showConfirmDialog(this, fields, "Report " + type + " Item", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;
        if (name.getText().trim().isBlank() || location.getText().trim().isBlank()) {
            JOptionPane.showMessageDialog(this, "Item name and location are required.");
            return;
        }
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String report = type + "|" + clean(name.getText()) + "|" + clean(category.getText()) + "|" +
                clean(description.getText()) + "|" + clean(location.getText()) + "|" + date + "|" + txtNickname.getText().trim();
        client.send("ITEM|" + report);
    }

    private void requestClaim() {
        int row = tblItems.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a found item first.");
            return;
        }
        String type = String.valueOf(itemModel.getValueAt(row, 0));
        if (!type.equals("FOUND")) {
            JOptionPane.showMessageDialog(this, "Claim requests are for found items.");
            return;
        }
        String note = JOptionPane.showInputDialog(this, "Enter details to help verify the item:");
        if (note != null && !note.trim().isBlank()) {
            appendChat("[System] Claim request feature is a demo placeholder. Request details: " + note);
        }
    }

    private void addItemRow(String report) {
        String[] parts = report.split("\\|", 7);
        if (parts.length == 7) itemModel.addRow(parts);
    }

    private String clean(String value) {
    return value.replace("|", "/")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
}

    private String timeNow() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void appendChat(String message) {
        txtChat.append(message + "\n");
        txtChat.setCaretPosition(txtChat.getDocument().getLength());
    }

    private void disconnect() {
        if (client != null) client.disconnect();
        connectionStatus.setText("● Disconnected");
        connectionStatus.setForeground(new Color(180, 55, 55));
        appendChat("[System] Disconnected.");
    }
}
