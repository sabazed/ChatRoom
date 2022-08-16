package chtroom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;


public class Server {

    private final HashMap<String, PrintWriter> users;
    private final LinkedList<StringBuilder> waiting;
    private int limit, current, queued;
    private Object lock;
    private ServerSocket server;

    private static HashMap<String, Integer> commands = new HashMap<>();;
    static {
        commands.put("/help", 0);
        commands.put("/exit", 1);
        commands.put("/send", 2);
        commands.put("/nick", 3);
    }

    private Server(int port) throws IOException {
        server = null;
        try {
            server = new ServerSocket(port);
            users = new HashMap<>();
            waiting = new LinkedList<>();
            lock = new Object();
            limit = 5;
            current = 0;
            queued = 0;

        }
        catch (IOException e) {
            System.out.println("Server initialization failed!");
            throw e;
        }
    }

    private void start() throws IOException {
        Thread connector = new Thread(new ConnectionHandler());
        connector.start();

        Thread dequeuer = new Thread(new QueueHandler());
        dequeuer.start();
    }

    private synchronized void connect(String nick, PrintWriter clientOutput) {
        users.put(nick, clientOutput);
        current++;
        sendMessage(nick + " is online!");
    }

    private synchronized void disconnect(StringBuilder nick) {
        if (users.containsKey(nick.toString())) {
            users.remove(nick.toString());
            current--;
            sendMessage(nick + " disconnected!");
            synchronized (lock) {
                lock.notify();
            }
        }
        else {
            waiting.remove(nick);
            queued--;
            System.out.println("[QUEUE] " + nick + "disconnected from waiting queue [" + queued + "]");
        }

    }

    private synchronized void queue(StringBuilder nick, PrintWriter clientOutput) {
        waiting.add(nick);
        clientOutput.println("You are being queued...\nUsers before you: " + queued);
        queued++;
        System.out.println("[QUEUE] User put into waiting queue [" + queued + "]");
    }

    private synchronized void sendMessage(String message) {
        String newMsg = "[JOIN/LEAVE] " + message + " [" + current + "/" + limit + "]";
        System.out.println(newMsg);
        for (PrintWriter out : users.values()) {
            out.println(newMsg);
        }
    }

    private synchronized void sendMessage(String message, String nick) {
        String newMsg = nick + ": " + message;
        System.out.println(newMsg);
        for (Entry<String, PrintWriter> entry : users.entrySet()) {
            if (entry.getKey().equals(nick)) continue;
            entry.getValue().println(newMsg);
        }
    }

    private boolean isRunning() {
        return server != null;
    }

    private class ClientHandler implements Runnable {

        private final Socket client;
        private StringBuilder nick;

        protected ClientHandler(Socket client) {
            super();
            this.client = client;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

                String nickTemp = reader.readLine();
                nick = new StringBuilder(nickTemp);
                while (users.containsKey(nickTemp) || waiting.contains(nick)) {
                    writer.println("Username taken, please enter a different name: ");
                    nickTemp = reader.readLine();
                    while (!Client.validateNick(nickTemp)) {
                        writer.println("Invalid nick! Please try again: ");
                        nickTemp = reader.readLine();
                    }
                }

                nick = new StringBuilder(nickTemp);
                while (limit == current || queued != 0) {
                    queue(nick, writer);
                    synchronized (nick) {
                        nick.wait();
                    }
                }

                writer.println("Connected to the server!");
                writer.println("Welcome: " + nick + "!");
                connect(nick.toString(), writer);

                String out;
                while ((out = reader.readLine()) != null) {
                    int cmd = commands.get(out.substring(0, 4));
                    switch (cmd) {
                        case 0 ->
                    }
                    sendMessage(out, nick.toString());
                }
            }
            catch (IOException | InterruptedException e) {
                disconnect(nick);
            }
        }
    }

    private class ConnectionHandler implements Runnable {

        public ConnectionHandler() {
            super();
        }

        @Override
        public void run() {
            try {
                System.out.println("[CONNECT] Waiting for connections...");
                while (isRunning()) {
                    Thread newClient = new Thread(new ClientHandler(server.accept()));
                    System.out.println("[CONNECT] Client connected!");
                    newClient.start();
                }
            }
            catch (IOException e) {
                System.out.println("[ERROR] IO Error occurred, aborting...");
                e.printStackTrace();
            }
        }

    }

    private class QueueHandler implements Runnable {

        public QueueHandler() {
            super();
        }

        @Override
        public void run() {
            try {
                while (isRunning()) {
                    while (limit == current || queued == 0) {
                        synchronized (lock) {
                            System.out.println("SLEEP");
                            lock.wait();
                            System.out.println("AWAKE");
                        }
                    }
                    StringBuilder lastQueued = waiting.removeFirst();
                    synchronized (lastQueued) {
                        lastQueued.notify();
                    }
                    queued--;
                }
            }
            catch (InterruptedException e) {
                System.out.println("[ERROR] Interrupted Exception occurred, aborting...");
                e.printStackTrace();
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
