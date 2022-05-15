package chtroom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

public class Server {

    private final HashMap<String, PrintWriter> users;
    private ServerSocket server;

    public Server(int port) throws IOException {
        server = null;
        try {
            server = new ServerSocket(port);
            users = new HashMap<>();
        }
        catch (IOException e) {
            System.out.println("Server initialization failed!");
            throw e;
        }
    }

    public synchronized void connect(String nick, PrintWriter clientOutput) {
        users.put(nick, clientOutput);
        sendMessage(nick + " is online!");
    }

    public synchronized void disconnect(String nick) {
        users.remove(nick);
        sendMessage(nick + " disconnected!");
    }

    public synchronized void sendMessage(String message) {
        String newMsg = "[JOIN/LEAVE] " + message;
        System.out.println(newMsg);
        for (PrintWriter out : users.values()) {
            out.println(newMsg);
        }
    }

    public synchronized void sendMessage(String message, String nick) {
        String newMsg = nick + ": " + message;
        System.out.println(newMsg);
        for (Entry<String, PrintWriter> entry : users.entrySet()) {
            if (entry.getKey().equals(nick)) continue;
            entry.getValue().println(newMsg);
        }
    }

    public void start() throws IOException {
        try {
            while (isRunning()) {
                System.out.println("Waiting for connections...");
                Thread newClient = new Thread(new ClientHandler(server.accept()));
                System.out.println("Client connected!");
                newClient.start();
            }
        }
        catch (IOException e) {
            throw e;
        }

    }

    public boolean isRunning() {
        return server != null;
    }

    private class ClientHandler implements Runnable {

        private final Socket client;
        private String nick;

        protected ClientHandler(Socket client) {
            super();
            this.client = client;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

                nick = reader.readLine();
                connect(nick, writer);

                String out;
                while ((out = reader.readLine()) != null) {
                    sendMessage(out, nick);
                }
            }
            catch (IOException e) {
                System.out.println(nick + " disconnected...");
            }
            finally {
                disconnect(nick);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the port number to start the server on (default 8080): ");
        String temp = scanner.nextLine();
        int port;
        if (temp.isBlank()) port = 8080;
        else port = Integer.parseInt(temp);
        scanner.close();
        try {
            Server server = new Server(port);
            server.start();
        }
        catch (IOException e) {
            System.out.println("IO Exception occurred!");
            e.printStackTrace();
        }
    }

}
