package com.mycompany.campuslostandfound.client;

import com.mycompany.campuslostandfound.gui.ClientView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ChatClient {

    private final String host;
    private final int port;
    private final String nickname;
    private final ClientView view;

    private volatile Socket socket;
    private volatile PrintWriter out;
    private volatile boolean connected = false;

    public ChatClient(String host, int port, String nickname, ClientView view) {
        this.host = host;
        this.port = port;
        this.nickname = nickname;
        this.view = view;
    }

    public void connect() {
        new Thread(() -> {
            try {
                Socket newSocket = new Socket();
                socket = newSocket;

                // Avoid waiting forever if the server is unavailable.
                newSocket.connect(new InetSocketAddress(host, port), 5000);

                out = new PrintWriter(newSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(newSocket.getInputStream()));

                out.println("JOIN|" + nickname);
                String firstMessage = in.readLine();

                if (firstMessage == null) {
                    view.onConnectionError("The server closed the connection before accepting the login.");
                    return;
                }

                if (firstMessage.startsWith("ERROR|")) {
                    view.onConnectionError(firstMessage.substring(6));
                    return;
                }

                if (!firstMessage.startsWith("INFO|")) {
                    view.onConnectionError("Unexpected response from server: " + firstMessage);
                    return;
                }

                connected = true;
                view.onConnected();
                view.onServerMessage(firstMessage);

                String line;
                while (connected && (line = in.readLine()) != null) {
                    view.onServerMessage(line);
                }

            } catch (SocketException e) {
                view.onConnectionError(
                    "Network connection failed (" + e.getMessage() + ").\n\n" +
                    "Check that ServerMain is running on " + host + ":" + port + ".\n" +
                    "If both programs are on this computer, use 127.0.0.1.\n" +
                    "If the server is on another computer, allow Java through Windows Firewall " +
                    "on the server and allow TCP port " + port + "."
                );
            } catch (SecurityException e) {
                view.onConnectionError(
                    "Java was denied permission to open a network connection. " +
                    "Check your Java security policy, antivirus, and Windows Firewall. Details: " +
                    e.getMessage()
                );
            } catch (IOException e) {
                view.onConnectionError(
                    "Could not connect to " + host + ":" + port + ". " + e.getMessage() +
                    "\nMake sure ServerMain is running and the IP address and port are correct."
                );
            } finally {
                connected = false;
                closeSocket();
            }
        }, "ChatClient-Reader").start();
    }

    public synchronized void send(String message) {
        if (connected && out != null) {
            out.println(message);
            if (out.checkError()) {
                connected = false;
            }
        }
    }

    public boolean isConnected() {
        Socket current = socket;
        return connected && current != null && current.isConnected() && !current.isClosed();
    }

    public void disconnect() {
        if (connected && out != null) {
            out.println("EXIT");
        }
        connected = false;
        closeSocket();
    }

    private void closeSocket() {
        try {
            Socket current = socket;
            if (current != null) {
                current.close();
            }
        } catch (IOException ignored) {
        }
    }
}
