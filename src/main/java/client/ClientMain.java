package client;

import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = new Client("172.30.117.59", 8000);
        client.run();
    }
}