package client;

import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = new Client("172.30.102.121", 8001);
        client.run();
    }
}