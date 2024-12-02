package network;

import persistence.dto.UserDTO;
import persistence.dao.UserDAO;

import java.io.*;
import java.net.Socket;

public class Threads extends Thread {
    private Socket socket;
    private UserDAO userDAO;
    private DataInputStream in;
    private DataOutputStream out;

    public Threads(Socket socket) {
        this.socket = socket;
        this.userDAO = new UserDAO();
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                // Read login credentials
                int id = in.readInt();
                int password = in.readInt();

                // Verify credentials
                UserDTO user = userDAO.findUser(id);
                boolean loginSuccess = false;

                if (user != null && user.getPassword() == password) {
                    loginSuccess = true;
                }

                // Send login result
                out.writeBoolean(loginSuccess);
                out.flush();

                if (loginSuccess) {
                    System.out.println("User " + id + " logged in successfully");
                } else {
                    System.out.println("Login failed for user " + id);
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}