package com.mycompany.campuslostandfound.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final ChatServer server;
    private final Socket socket;

    private PrintWriter out;
    private String nickname;
    private boolean registered = false;

    ClientHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public String getNickname() {
        return nickname;
    }

    public synchronized void send(String message) {

        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public void run() {

        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            )
        ) {

            out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            // Wait for the client's JOIN message.
            String first = in.readLine();

            if (first == null || !first.startsWith("JOIN|")) {
                return;
            }

            String requestedName = first.substring(5).trim();

            if (requestedName.isBlank()
                    || requestedName.length() > 30
                    || requestedName.contains("|")
                    || !server.registerNickname(requestedName)) {

                send("ERROR|Nickname is invalid or already in use.");
                return;
            }

            nickname = requestedName;
            registered = true;

            server.addClient(this);

            send("INFO|Connected to Campus Lost and Found server.");

            // Send previous reports to this newly connected client.
            for (String report : server.getItemReports()) {
                send("ITEM|" + report);
            }

            server.broadcast(
                    "CHAT|Server|" + nickname + " joined the chat."
            );

            server.broadcast(
                    "USERLIST|" + server.getUserList()
            );

            String line;

            while ((line = in.readLine()) != null) {

                if (line.equals("EXIT") || line.equals("QUIT")) {
                    break;
                }

                if (line.startsWith("CHAT|")) {

                    String message = line.substring(5).trim();

                    if (!message.isBlank()) {

                        server.broadcast(
                                "CHAT|" + nickname + "|" + message
                        );
                    }

                } else if (line.startsWith("ITEM|")) {

                    String report = line.substring(5);

                    String[] parts = report.split("\\|", 7);

                    if (parts.length == 7
                            && (parts[0].equals("LOST")
                            || parts[0].equals("FOUND"))) {

                        server.addItemReport(report);

                    } else {

                        send("ERROR|Invalid item report.");
                    }

                } else if (line.startsWith("RENAME|")) {

                    String newName = line.substring(7).trim();

                    if (newName.length() > 30
                            || !server.renameNickname(
                                    nickname, newName
                            )) {

                        send(
                                "ERROR|Nickname is invalid or already in use."
                        );

                    } else {

                        String oldName = nickname;

                        nickname = newName;

                        server.broadcast(
                                "CHAT|Server|" + oldName
                                + " is now " + newName + "."
                        );

                        server.broadcast(
                                "USERLIST|" + server.getUserList()
                        );

                        send(
                                "INFO|Nickname changed to " + newName
                        );
                    }
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Client disconnected: " + e.getMessage()
            );

        } finally {

            if (registered) {
                server.unregister(this, nickname);
            }

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}