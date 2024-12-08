package client;

import network.Message;
import common.Packet;
import view.StudentViewer;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final String ip;
    private final int port;
    private Socket cliSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private BufferedReader br;
    Scanner sc = new Scanner(System.in);
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
                System.out.print("로그인을 하시려면 Y/y를 입력하시고 프로그램을 종료하시려면 Q/q를 입력하세요. : ");
                char userChoice = sc.next().charAt(0);

                if(userChoice == 'Y' || userChoice == 'y'){
                    System.out.print("ID를 입력하세요: ");
                    String id = br.readLine();
                    System.out.print("비밀번호를 입력하세요: ");
                    String password = br.readLine();

                    String newData = id + "," + password;
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.Login, Packet.NOT_USED, newData)));
                    out.flush();

                    Message rxMsg = Message.readMessage(in);
                    Message.printMessage(rxMsg);

                    System.out.println("서버로부터 로그인 결과 수신");
                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        if (rxMsg.getData().equals("학생")) {
                            studentRun();
                            return;
                        } else if (rxMsg.getData().equals("관리자")) {
                            //adminRun();
                        } else {
                            System.out.println("에러");
                        }
                    } else {
                        System.out.println("로그인 실패!");
                    }

                }
                else if (userChoice == 'Q' || userChoice == 'q') {
                    System.out.println("프로그램을 종료합니다......");
                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.NOT_USED, Packet.END_CONNECT, "")));
                    out.flush();
                    break;
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
            if (sc != null) sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void studentRun() throws IOException {
        while (true) {
            int studentMenu = StudentViewer.viewStudentPage();

            if (studentMenu == 1) { // 선발일정 조회
                out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_SCHEDULE, Packet.NOT_USED, " d")));
                out.flush();

                Message rxMsg = Message.readMessage(in);
                Message.printMessage(rxMsg);

                byte type = rxMsg.getType();
                byte detail = rxMsg.getDetail();

                if (type == Packet.RESULT && detail == Packet.SUCCESS) {
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
            } else if (studentMenu == 4) { // 생활관 비용 확인 및 납부
                out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_PAY_DORMITORY, Packet.NOT_USED, "")));
                out.flush();

                Message rxMsg = Message.readMessage(in);
                Message.printMessage(rxMsg);

                byte type = rxMsg.getType();
                byte detail = rxMsg.getDetail();
                if (type == Packet.RESPONSE && detail == Packet.SUCCESS) {
                    String data = rxMsg.getData();
                    if (data != null && !data.isEmpty()) {
                        String[] parts = data.split(",");
                        String roomFee = parts[0];
                        String mealFee = parts[1];
                        String totalFee = parts[2];
                        String paymantStatus = parts[3];
                        System.out.println(roomFee + ", " + mealFee + ", " + totalFee + ", " + paymantStatus);
                    }

                    System.out.println("납부하시겠습니까? (Y/N): ");
                    char userChoice = sc.next().charAt(0);

                    if(userChoice == 'Y' || userChoice == 'y'){
                        out.write(Packet.makePacket(Message.makeMessage(Packet.RESPONSE, Packet.CHECK_PAY_DORMITORY, Packet.SUCCESS, "")));
                        out.flush();
                    } else if (userChoice == 'N' || userChoice == 'n') {
                        out.write(Packet.makePacket(Message.makeMessage(Packet.RESPONSE, Packet.CHECK_PAY_DORMITORY, Packet.FAIL, "")));
                        out.flush();
                    }

                    rxMsg = Message.readMessage(in);
                    Message.printMessage(rxMsg);

                    if(rxMsg.getDetail() == Packet.SUCCESS){
                        System.out.println("납부가 정상적으로 처리되었습니다.");
                    } else if (rxMsg.getDetail() == Packet.FAIL) {
                        System.out.println("미납부 상태입니다.");
                    }

                } else if (type == Packet.RESULT && detail == Packet.FAIL) {
                    System.out.println(rxMsg.getData());
                }




            } else if (studentMenu == 6) { // 퇴사 신청
                System.out.println("퇴사 신청 메뉴 입니다.");
                System.out.println("환불 받으실 은행 이름, 계좌 번호, 퇴사 신청 사유를 입력해주세요.");
                System.out.print("환불 받으실 은행 이름: ");
                sc.nextLine();
                String bankName = sc.nextLine();
                System.out.print("계좌 번호: ");
                String accountNumber = sc.nextLine();
                System.out.print("퇴사 신청 사유: ");
                String reason = sc.nextLine();

                String newData = bankName + "," + accountNumber + "," + reason;
                out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.REQUEST_WITHDRAWAL, Packet.NOT_USED, newData)));
                out.flush();

                Message rxMsg = Message.readMessage(in);
                Message.printMessage(rxMsg);

                if (rxMsg.getDetail() == Packet.SUCCESS){
                    System.out.println("퇴사 신청이 정상적으로 처리 되었습니다.");
                }else{
                    System.out.println(rxMsg.getData());
                    System.out.println("퇴사 신청에 실패하였습니다.");
                }


            } else if (studentMenu == 7) { //환불 결과 조회
                out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_REFUND, Packet.NOT_USED, "")));
                out.flush();

                Message rxMsg = Message.readMessage(in);
                Message.printMessage(rxMsg);

                if(rxMsg.getDetail() == Packet.SUCCESS){
                    if(rxMsg.getData().equals("승인")){
                        System.out.println("환불 처리 되었습니다. ");
                    } else if (rxMsg.getData().equals("취소")) {
                        System.out.println("환불 처리가 되어있지 않습니다. ");
                    }
                } else if (rxMsg.getDetail() == Packet.FAIL) {
                    System.out.println(rxMsg.getData());
                }


            } else if (studentMenu == 8) {
                System.out.println("프로그램을 종료합니다......");
                txMsg = Message.makeMessage(Packet.RESULT, Packet.NOT_USED,
                        Packet.END_CONNECT, "");
                packet = Packet.makePacket(txMsg);
                out.write(packet);
                out.flush();
                return;
            }

        }

    }
}