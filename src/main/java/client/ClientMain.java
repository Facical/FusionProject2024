package client;

import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = new Client("127.0.0.1", 8000);
        client.run();
    }
}