import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    //we implement Runnable. It means, this class can be passed to
    //a thread or a thread pool and can be executed concurrently
    //alongside other runnable classes that implemented runnable interface.


    //*We need list of clients that connected and that is why we need array
    private ArrayList<ConnectionHandler> connections;

    private ServerSocket server;
    private boolean done;
    private ExecutorService pool; //thread-pool

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }


    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                //whenever we create new client
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }

    }


    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    //to shut down the connection/server or connection of particular client
    @SneakyThrows
    public void shutdown() {
        done = true;
        pool.shutdown();
        if (!server.isClosed()) {
            server.close();
        }
        for (ConnectionHandler ch : connections) {
            ch.shutdown();
        }
    }

    //this class going to be what handles client connection
    //so, we're going to pass the client to it
    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in; //get from the socket when client send something
        private PrintWriter out; //when we want to something to client
        private String nickname;

// We need to handle multiple clients concurrently and because of that we're going to define constructor here

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {

            try {
                //First we are going to initialize in and out :
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //if you need to send something to client just need to write this ---
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined the chat!");
                //for this particular client we want always have a loop that asks new messages
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick ")) {
                        out.println(message);
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname);
                        }else {
                            out.println("No nickname provided!");
                        }
                    }else if (message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat!");
                        shutdown();
                    }else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        //to be able to send message to the client via the handler we need to implement function
        public void sendMessage(String message) {
            out.println(message);
        }

        @SneakyThrows
        public void shutdown() {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }


}
