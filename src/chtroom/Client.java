package chtroom;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    private Socket client = null;

    public Client() {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the [address]:<port> of the server to connect (default localhost:8080): ");

        while (client == null) {
            String address = scanner.nextLine();

            if (address.isBlank()) address = "localhost:8080";
            String ip = null;
            int port = 0;
            boolean valid = false;

            while (!valid) {
                try {
                    int semicolon = address.indexOf(':');
                    if (address.charAt(0) == 'h') {
                        semicolon = address.lastIndexOf(':');
                    }
                    ip = address.substring(0, semicolon);
                    port = Integer.parseInt(address.substring(semicolon + 1));
                    valid = true;
                } catch (Exception e) {
                    System.out.println("Wrong input! Please enter [address]:<port> input: ");
                    address = scanner.nextLine();
                    if (address.isBlank()) address = "localhost:8080";
                }
            }

            System.out.println("Establishing connection...");
            try {
                client = new Socket(ip, port);
            } catch (IOException e) {
                System.out.println("Couldn't find server, please try again: ");
            }

        }

        start();

    }

    public void start() {

        try {

            PrintWriter output = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner in = new Scanner(System.in);

            System.out.println("Enter your nickname: ");
            String nick = "";
            nick = in.nextLine();
            while (!validateNick(nick)) {
                System.out.println("Invalid nick! Please try again: ");
                nick = in.nextLine();
            }
            output.println(nick);

            Thread reader = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                        String message = null;
                        while ((message = input.readLine()) != null) {
                            System.out.println(message);
                        }
                    }
                    catch (IOException e) {
                        System.out.println("Connection interrupted!");
                    }
                    finally {
                        interrupt();
                        in.close();
                        output.close();
                    }
                }
            };

            reader.start();

            while (true) {
                if (reader.isInterrupted()) return;
                String message = in.nextLine();
                output.println(message);
            }

        }

        catch (IOException e) {
            System.out.println("I/O Exception occurred!");
        }

    }

    protected static boolean validateNick(String nick) {
        return !nick.isBlank() && nick.indexOf(':') == -1;
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
    }

}
