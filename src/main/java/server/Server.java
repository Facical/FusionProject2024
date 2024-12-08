package server;

import network.Threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 8001;
    ServerSocket serverSocket;
    Socket socket = null;

    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server Running on port " + PORT);

            while (true) {
                socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                Threads thread = new Threads(socket);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}