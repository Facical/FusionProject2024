// Threads.java
package network;

import common.Packet;
import dao.ScheduleDAO;
import dto.ScheduleDTO;
import dto.UserDTO;
import dao.UserDAO;


import java.io.*;
import java.net.Socket;

public class Threads extends Thread {
    private Socket socket;
    private UserDTO userDTO;
    private UserDAO userDAO;
    private ScheduleDAO scheduleDAO;
    private ScheduleDTO scheduleDTO;
    private DataInputStream in;
    private DataOutputStream out;
    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;
    Message txMsg = null;
    Message rxMsg = null;

    public Threads(Socket socket) {
        this.socket = socket;
        this.userDAO = new UserDAO();
        this.scheduleDAO = new ScheduleDAO();
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //
            txMsg = Message.makeMessage(Packet.REQUEST, Packet.Login, Packet.NOT_USED, "Login Request");
            packet = Packet.makePacket(txMsg);
            out.write(packet);
            out.flush();


            while(true) {
                rxMsg = new Message();
                header = new byte[Packet.LEN_HEADER];
                in.read(header);
                Message.makeMessageHeader(rxMsg, header);
                body = new byte[rxMsg.getLength()];
                in.read(body);
                Message.makeMessageBody(rxMsg, body);
                Message.printMessage(rxMsg);
                byte type = rxMsg.getType();

                System.out.println("test" + type);

                switch(type) {

                    case Packet.REQUEST:
                        /*rxMsg = new Message();
                        header = new byte[Packet.LEN_HEADER];
                        in.read(header);
                        Message.makeMessageHeader(rxMsg, header);
                        body = new byte[rxMsg.getLength()];
                        in.read(body);
                        Message.makeMessageBody(rxMsg, body);
                        Message.printMessage(rxMsg);*/
                        byte code = rxMsg.getCode();
                        System.out.println("코드 받음");

                        switch(code){
                            case Packet.CHECK_SCHEDULE:
                                System.out.println("스케쥴 조회전");
                                ScheduleDTO schedule = scheduleDAO.getSchedule();

                                String newData = schedule.getPeriodName() + "," + schedule.getStartDate() +"," + schedule.getEndDate();
                                System.out.println(newData);

                                txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.SUCCESS, newData);
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                return; // 이 부분 수정
                            //break;
                        }

                    case Packet.RESPONSE:
                        System.out.println("로그인 응답 정보 도착");
                        String data = rxMsg.getData();
                        if(data != null && !data.isEmpty()) {
                            String[] parts = data.split(",");
                            String id = parts[0];
                            String password = parts[1];

                            UserDTO user = userDAO.findUser(Integer.parseInt(id));
                            boolean loginSuccess = (user != null) &&
                                    String.valueOf(user.getPassword()).equals(password);


                            if(loginSuccess) {
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.Login,
                                        Packet.SUCCESS, user.getRole());

                                System.out.println("User " + id + " logged in successfully");
                            } else {
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.Login,
                                        Packet.FAIL, "Login Failed");
                                System.out.println("Login failed for user " + id);
                            }
                            packet = Packet.makePacket(txMsg);
                            out.write(packet);
                            out.flush();
                            //return;
                        }
                        break;

                    default:
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