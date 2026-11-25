package com.mycompany.campuslostandfound.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    private final int port;

    private final Set<ClientHandler> clients =
            ConcurrentHashMap.newKeySet();

    private final Set<String> nicknames =
            ConcurrentHashMap.newKeySet();

    private final List<String> itemReports =
            new CopyOnWriteArrayList<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {

        System.out.println(
                "Campus Lost and Found server starting on port " + port
        );

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is running!");

            while (true) {

                Socket socket = serverSocket.accept();

                ClientHandler handler =
                        new ClientHandler(this, socket);

                Thread thread = new Thread(
                        handler,
                        "Client-" + socket.getRemoteSocketAddress()
                );

                thread.start();
            }

        } catch (IOException e) {

            System.out.println(
                    "Server error: " + e.getMessage()
            );
        }
    }

    synchronized boolean registerNickname(String nickname) {

        if (nickname == null || nickname.isBlank()
                || nickname.contains("|")) {
            return false;
        }

        return nicknames.add(nickname.toLowerCase());
    }

    synchronized boolean renameNickname(
            String oldName,
            String newName
    ) {

        if (newName == null || newName.isBlank()
                || newName.contains("|")) {
            return false;
        }

        String oldKey = oldName.toLowerCase();
        String newKey = newName.toLowerCase();

        if (oldKey.equals(newKey)) {
            return true;
        }

        if (!nicknames.add(newKey)) {
            return false;
        }

        nicknames.remove(oldKey);

        return true;
    }

    void addClient(ClientHandler handler) {
        clients.add(handler);
    }

    void unregister(ClientHandler handler, String nickname) {

        clients.remove(handler);

        if (nickname != null) {
            nicknames.remove(nickname.toLowerCase());
        }

        broadcast("USERLIST|" + getUserList());

        if (nickname != null) {
            broadcast(
                    "CHAT|Server|" + nickname + " left the chat."
            );
        }
    }

    String getUserList() {

        return clients.stream()
                .map(ClientHandler::getNickname)
                .filter(n -> n != null)
                .sorted()
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    void broadcast(String message) {

        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    void addItemReport(String report) {

        itemReports.add(report);

        // Send the new report to all connected clients.
        broadcast("ITEM|" + report);
    }

    List<String> getItemReports() {
        return itemReports;
    }
}