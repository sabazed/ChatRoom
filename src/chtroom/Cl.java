package chtroom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Chat room client
 * @author adminitartor
 *
 */
public class Cl {
    /*
     * socket
     * The bottom layer encapsulates the TCP protocol and uses it for network communication
     */
    private Socket socket;
    /**
     * Constructor to initialize the client
     * @throws Exception
     */
    public Cl() throws Exception{
        try {
            /*
             * Two parameters need to be passed in when instantiating the Socket
             * Parameter 1: The ip address of the server
             * Parameter 2: The port number of the server
             * You can connect to the server computer through IP, through
             * The port is connected to the computer running on the server
             * Server application.
             * The process of creating a Socket is the process of connecting, so
             * If the server does not respond, an exception will be thrown here
             */
            System.out.println("Connecting to the server...");
            socket = new Socket(
                    "localhost",8888
            );
            System.out.println("Connected to the server!");
        } catch (Exception e) {
            System.out.println("Client initialization failed!");
            throw e;
        }
    }
    /**
     * Method to start the client
     */
    public void start(){
        try {
            Scanner scanner = new Scanner(System.in);
            // first ask the user to enter a nickname
            String nickName = null;
            while(true){
                System.out.println("Please enter nickname:");
                nickName = scanner.nextLine();
                if(nickName.length()>0){
                    break;
                }
                System.out.println("Enter at least one character.");
            }

            /*
             * If you want to send information to the server, you need to pass the socket
             * Get the output stream, the data written through the stream will pass through
             * The network is sent to the server.
             */
            OutputStream out
                    = socket.getOutputStream();

            OutputStreamWriter osw
                    = new OutputStreamWriter(out,"UTF-8");

            PrintWriter pw
                    = new PrintWriter (osw, true);
            //First send the nickname to the server
            pw.println(nickName);
            System.out.println("Welcome: "+nickName+"! Let's chat!");



            //First start the thread that accepts the message from the server
            ServerHandler handler = new ServerHandler();
            Thread t = new Thread(handler);
            t.start();






            while(true){
                String message = scanner.nextLine();
                pw.println(message);
            }



        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public static void main(String[] args) {
        try {
            Cl client = new Cl();
            client.start();
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
    /**
     * This thread is used to receive and process messages sent by the server
     * @author adminitartor
     *
     */
    private class ServerHandler
            implements Runnable{

        public void run(){
            try {

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),"UTF-8"
                        )
                );

                String message = null;
                while((message = br.readLine())!=null){
                    System.out.println(message);
                }


            } catch (Exception e) {
                // TODO: handle exception
            }
        }

    }
}

