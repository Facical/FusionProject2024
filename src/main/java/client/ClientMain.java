package client;

import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = new Client("192.168.0.7", 8001);
        client.run();
    }
}