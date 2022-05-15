package chtroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Chat room server
 * @author adminitartor
 *
 */
public class Chat {
    /*
     * java.net.ServerSocket
     * Used to apply to the system for a service port (the client is through this port
     * to establish a connection with the server application)
     * Listen on the service port to accept connections from clients.
     */
    private ServerSocket server;
    /*
     * This collection holds the output streams of all clients for broadcasting messages
     */
    private Map<String,PrintWriter> allOut;

    public Chat() throws Exception{
        try {
            /*
             * Apply for a service port when creating, it cannot be used with the system
             * Port conflicts already used by other applications
             * Otherwise throw:
             *Error in AddressAlreadyInUser
             */
            server = new ServerSocket(8888);

            allOut = new HashMap<String,PrintWriter>();
        } catch (Exception e) {
            System.out.println("Server initialization failed!");
            throw e;
        }
    }
    /**
     * Store the output stream of a given client into a shared collection
     * @param nickName
     * @param out
     */
    public synchronized void addOut(String nickName,PrintWriter out){
        allOut.put(nickName,out);
    }
    /**
     * Remove the given client's output stream from the shared set
     * @param nickName
     */
    public synchronized void removeOut(String nickName){
        allOut.remove(nickName);
    }
    /**
     * Send the given message to all clients
     * @param message
     */
    public synchronized void sendMessage(String message){
        for(PrintWriter out : allOut.values()){
            out.println(message);
            System.out.println(message);
        }
    }
    /**
     * Send the specified message to the client with the specified nickname
     * @param nickName
     * @param message
     */
    public synchronized void sendMessage(String nickName,String message){
        PrintWriter out = allOut.get(nickName);
        if(out != null){
            out.println(message);
        }
    }


    public void start(){
        try {
            /*
             * The accept method of ServerSocket is a
             * Blocking method, the function is to listen to the requested service port
             * Until a client communicates with the server through this port
             * After the connection is established, the accept method will be executed.
             * And return a Socket instance, through this Socket
             * Can interact with the client that just established the connection
             */
            while(true){
                System.out.println("Waiting for client connection...");
                Socket socket = server.accept();
                System.out.println("A client is connected!");

                //Start the thread to handle the client interaction
                ClientHandler handler
                        = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();

            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public static void main(String[] args) {
        try {
            Chat server = new Chat();
            server.start();
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    /**
     * This thread is used to handle a client connected to the server
     * Works interactively.
     * @author adminitartor
     *
     */
    private class ClientHandler
            implements Runnable{
        //The current thread interacts with the client through the Socket
        private Socket socket;
        //client's IP address
        private String host;
        //User's Nickname
        private String nickName;

        public ClientHandler(Socket socket){
            super();
            this.socket = socket;
        }

        public void run(){
            PrintWriter pw = null;
            try {
                //Get the client IP address
                InetAddress address
                        = socket.getInetAddress();
                //Get the string form of the client's IP address
                host = address.getHostAddress();


                /*
                 * If you want to read the data sent by the client, you need to pass
                 * Socket gets the input stream.
                 */
                InputStream in
                        = socket.getInputStream();

                InputStreamReader isr
                        = new InputStreamReader(in,"UTF-8");

                BufferedReader br
                        = new BufferedReader(isr);

                //First read the nickname sent by the client
                nickName = br.readLine();
                sendMessage(nickName+"Online...");

                /*
                 * Get the output stream through Socket to send the message
                 * send to client
                 */
                pw = new PrintWriter (
                        new OutputStreamWriter(
                                socket.getOutputStream(),"UTF-8"
                        ),true
                );
                //Store the client's output stream into the shared collection
                addOut(nickName,pw);



                /*
                 * The br.readLine method reads the data sent by the remote computer
                 * When a line of string is used, after the remote computer is disconnected, its different
                 * The operating system of this will affect the result of this readLine method.
                 * When the windows client disconnects:
                 * The br.readLine method will throw an exception directly
                 *
                 * When the linux client disconnects:
                 * br.readLine method will return null
                 *
                 */
                String message = null;
                while((message = br.readLine())!=null){
//					System.out.println(host+"说:"+message);
//					pw.println(host+"说:"+message);
                    //First determine whether it is a private chat
                    if(message.startsWith("@")){
                        //First find the position of ":"
                        int index = message.indexOf(":");
                        if(index<0){
                            // inform the current client that the format is incorrect
                            pw.println("The format of private chat is wrong, private chat format:@nickname:chat content");
                        }else{
                            //Get the other party's nickname
                            String toNick = message.substring(1,index);
                            //Check if the user exists:
                            if(allOut.containsKey(toNick)){
                                //get chat content
                                message = message.substring(index+1);
                                sendMessage(toNick,nickName+"Say to you:"+message);
                            }else{
                                pw.println("No user found:"+toNick);
                            }

                        }
                    }else{
                        //Broadcast to all clients
                        sendMessage(nickName+"说:"+message);
                    }
                }
            } catch (Exception e) {

            } finally{
                //Processing after the client disconnects

                //Remove the client's output stream from the shared collection
                removeOut(nickName);

                sendMessage(nickName+"Offline...");


                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }
    }

}




