package client;

import network.Message;
import common.Packet;
import view.StudentViewer;
import view.AdminViewer;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Client {
    private final String ip;
    private final String port;
    private Socket cliSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private BufferedReader br;

    Message txMsg = null;  // 전송용 메시지
    Message rxMsg = null;  // 수신용 메시지
    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;

    // 날짜 형식 지정을 위한 포매터
    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = String.valueOf(port);
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
                                adminRun();
                            } else {
                                System.out.println("에러: 알 수 없는 사용자 유형");
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

    // 학생 메뉴 실행
    private void studentRun() throws IOException {
        while (true) {
            int studentMenu = StudentViewer.viewStudentPage();
            switch(studentMenu) {
                case 1: // 선발 일정 및 비용 확인
                    // 서버에 일정 조회 요청
                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.CHECK_SCHEDULE,
                            Packet.NOT_USED, "일정 조회 요청");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버로부터 응답 수신
                    rxMsg = new Message();
                    header = new byte[Packet.LEN_HEADER];
                    in.read(header);
                    Message.makeMessageHeader(rxMsg, header);
                    body = new byte[rxMsg.getLength()];
                    in.read(body);
                    Message.makeMessageBody(rxMsg, body);

                    // 응답 처리
                    if (rxMsg.getType() == Packet.RESULT) {
                        if (rxMsg.getDetail() == Packet.SUCCESS) {
                            // 세미콜론으로 구분된 여러 일정 처리
                            String[] schedules = rxMsg.getData().split(";");
                            for (String schedule : schedules) {
                                String[] parts = schedule.split(",");
                                if (parts.length >= 3) {
                                    System.out.println("기간명: " + parts[0]);
                                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    System.out.println("시작일: " + parts[1]);
                                    System.out.println("종료일: " + parts[2]);
                                    System.out.println("---------------");
                                }
                            }
                        } else {
                            System.out.println("일정 조회 실패: " + rxMsg.getData());
                        }
                    }
                    break;

                case 0: // 종료
                    return;
            }
        }
    }

    // 관리자 메뉴 실행
    private void adminRun() throws IOException {
        while (true) {
            int adminMenu = AdminViewer.viewAdminPage();
            switch(adminMenu) {
                case 1: // 선발 일정 등록
                    System.out.println("=== 선발 일정 등록 ===");
                    System.out.print("기간명 입력: ");
                    String periodName = br.readLine();
                    System.out.print("시작일 입력 (yyyy-MM-dd HH:mm:ss): ");
                    String startDate = br.readLine();
                    System.out.print("종료일 입력 (yyyy-MM-dd HH:mm:ss): ");
                    String endDate = br.readLine();

                    String scheduleData = String.format("%s,%s,%s",
                            periodName, startDate, endDate);

                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.REGISTER_SCHEDULE,
                            Packet.NOT_USED, scheduleData);
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버로부터 응답 수신
                    rxMsg = new Message();
                    header = new byte[Packet.LEN_HEADER];
                    in.read(header);
                    Message.makeMessageHeader(rxMsg, header);
                    body = new byte[rxMsg.getLength()];
                    in.read(body);
                    Message.makeMessageBody(rxMsg, body);

                    // 등록 결과 출력
                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        System.out.println("일정 등록 성공!");
                    } else {
                        System.out.println("일정 등록 실패: " + rxMsg.getData());
                    }
                    break;

                case 0: // 종료
                    return;
            }
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
}