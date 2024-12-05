package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = new Client("172.30.117.59", 8000);
        client.run();
    }
}