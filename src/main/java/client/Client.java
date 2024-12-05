package client;

import dto.UserDTO;
import network.Message;
import common.Packet;
import view.StudentViewer;

import java.io.*;
import java.net.Socket;

public class Client {
    private final String ip;
    private final int port;
    private Socket cliSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private BufferedReader br;
    Message txMsg = null;
    Message rxMsg = null;
    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
        try {
            cliSocket = new Socket(ip, port);
            out = new DataOutputStream(cliSocket.getOutputStream());
            in = new DataInputStream(cliSocket.getInputStream());
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            while (true) {
                // 서버로부터 메시지 수신
                rxMsg = new Message();
                header = new byte[Packet.LEN_HEADER];
                in.read(header);
                Message.makeMessageHeader(rxMsg, header);
                body = new byte[rxMsg.getLength()];
                in.read(body);
                Message.makeMessageBody(rxMsg, body);
                Message.printMessage(rxMsg);

                byte type = rxMsg.getType();
                switch (type) {
                    case Packet.REQUEST:
                        System.out.println("서버가 로그인 정보 요청");
                        System.out.print("ID를 입력하세요: ");
                        String id = br.readLine();
                        System.out.print("비밀번호를 입력하세요: ");
                        String password = br.readLine();

                        String newData = id + "," + password;
                        txMsg = Message.makeMessage(Packet.RESPONSE, Packet.Login,
                                Packet.NOT_USED, newData);
                        packet = Packet.makePacket(txMsg);
                        out.write(packet);
                        out.flush();
                        break;

                    case Packet.RESULT:
                        System.out.println("서버로부터 로그인 결과 수신");
                        if (rxMsg.getDetail() == Packet.SUCCESS) {
                            if (rxMsg.getData().equals("학생")) {
                                studentRun();
                            } else if (rxMsg.getData().equals("관리자")) {
                                //adminRun();
                            } else {
                                System.out.println("에러");
                            }


                        } else {
                            System.out.println("로그인 실패!");
                        }
                        return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (br != null) br.close();
            if (out != null) out.close();
            if (in != null) in.close();
            if (cliSocket != null) cliSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void studentRun() throws IOException {
        while (true) {
            int studentMenu = StudentViewer.viewStudentPage();
            if (studentMenu == 1) {
                txMsg = new Message();
                txMsg = Message.makeMessage(Packet.REQUEST, Packet.CHECK_SCHEDULE,
                        Packet.NOT_USED, " d");
                packet = Packet.makePacket(txMsg);
                out.write(packet);
                out.flush();

                rxMsg = new Message();
                header = new byte[Packet.LEN_HEADER];
                in.read(header);
                Message.makeMessageHeader(rxMsg, header);
                body = new byte[rxMsg.getLength()];
                in.read(body);
                Message.makeMessageBody(rxMsg, body);
                Message.printMessage(rxMsg);

                byte type = rxMsg.getType();
                byte detail = rxMsg.getDetail();
                switch (type) {
                    case Packet.RESULT:
                        switch (detail) {
                            case Packet.SUCCESS:
                                String data = rxMsg.getData();
                                if (data != null && !data.isEmpty()) {
                                    String[] parts = data.split(",");
                                    String periodName = parts[0];
                                    String startDate = parts[1];
                                    String endDate = parts[2];
                                    System.out.print(periodName + startDate + endDate);
                                    System.out.println();
                                }
                        }
                }
            }
        }

    }
}