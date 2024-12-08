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
    private static int loggedInUserId = -1; // 어디서든 참조 가능
    Message txMsg = null;  // 전송용 메시지
    Message rxMsg = null;  // 수신용 메시지
    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;
    private int studentId;
//    // 날짜 형식 지정을 위한 포매터
//    private static final SimpleDateFormat dateFormat =
//            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
            int id = -1; // 초기값만 선언하고 실제 사용은 나중에
            while (true) {
                // 서버로부터 메시지 수신
                Message rxMsg = Message.readMessage(in);
                Message.printMessage(rxMsg);

                byte type = rxMsg.getType();
                switch (type) {
                    case Packet.REQUEST:
                        System.out.println("서버가 로그인 정보 요청");
                        System.out.print("ID를 입력하세요: ");
                        String idInput = br.readLine();
                        if (idInput == null || idInput.trim().isEmpty()) {
                            System.out.println("올바른 ID를 입력해주세요.");
                            continue; // 입력이 유효하지 않으면 다시 요청
                        }

                        id = Integer.parseInt(idInput);  // 이 시점에서만 parseInt 수행

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
                            String data = rxMsg.getData();
                            String[] parts = data.split(",");
                            int userId = Integer.parseInt(parts[0]); // 사용자 ID 저장
                            String userRole = parts[1];

                            if (userRole.equals("학생")) {
                                this.studentId = userId; // 클래스 멤버 변수에 저장
                                loggedInUserId = userId;
                                studentRun();
                            } else if (userRole.equals("관리자")) {
                                adminRun();
                            }
                        }
                        break;
                    // return;
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
        System.out.println("로그인한 user_id : " + loggedInUserId);

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
                                if (parts.length >= 5) {
                                    System.out.println("기간명: " + parts[0]);
                                    System.out.println("시작일: " + parts[1] + " " + parts[2]);
                                    System.out.println("종료일: " + parts[3] + " " + parts[4]);
                                    System.out.println("---------------");
                                }
                            }
                        } else {
                            System.out.println("일정 조회 실패: " + rxMsg.getData());
                        }
                    }
                    break;
                case 2: // 1.2 기능
                    System.out.println("=== 입사 신청 ===");
                    System.out.println("1지망 생활관 입력");
                    String firstDormitory = br.readLine();
                    System.out.println("1지망 생활관의 식사 입력 (5일식, 7일식) : ");
                    String firstDormitoryMeal = br.readLine();
                    System.out.println("2지망 생활관 입력");
                    String secondDormitory = br.readLine();
                    System.out.println("2지망 생활관의 식사 입력 (5일식, 7일식) : ");
                    String secondDormitoryMeal = br.readLine();

                    String newData = firstDormitory + "," + firstDormitoryMeal + "," + secondDormitory + "," + secondDormitoryMeal;
                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.APPLY_ADMISSION,
                            Packet.NOT_USED, newData);
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

                    if (rxMsg.getType() == Packet.RESULT){
                        System.out.println("입사 신청 성공!");
                    }else{
                        System.out.println("입사 신청 실패");
                    }
                    break;
                case 3: // 1.3 기능
                    //합격 여부 및 호실 확인
                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.CHECK_ADMISSION,
                            Packet.NOT_USED, "합격 여부 및 호실 확인 조회 요청");
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

                    String data = rxMsg.getData();
                    String[] parts = data.split(",");

                    String roomId = parts[0];
                    String status = parts[1];

                    if (rxMsg.getType() == Packet.RESULT){
                        System.out.println("합격 여부 : " + status);
                        System.out.println("호실 확인 : " + roomId);
                    }else{
                        System.out.println("합격 여부 및 호실 확인 실패");
                    }
                    return;

                case 4: // 1.4 기능

                case 5: // 1.5 기능

                case 6: // 1.6 기능

                case 7: // 1.7 기능

                case 8: // 1.8 기능

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
                case 2: // 1.2 기능
                    System.out.println("=== 생활관 사용료 및 급식비 등록 ===");
                    System.out.println("생활관 입력 :");
                    String dormitoryName = br.readLine();
                    System.out.println("생활관 사용료 입력 :");
                    String dormitoryUsageFee = br.readLine();
                    System.out.println("생활관 급식비 등록 :");
                    String dormitoryMealFee = br.readLine();
                    String newData = dormitoryName + "," + dormitoryUsageFee + "," + dormitoryMealFee;
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.REGISTER_FEE,
                            Packet.NOT_USED, newData);
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

                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        System.out.println("생활관 사용료 및 급식비 등록 성공!");
                    } else {
                        System.out.println("생활관 사용료 및 급식비 등록 실패: " + rxMsg.getData());
                    }
                    break;
                case 3: // 1.3 기능

                case 4: // 1.4 기능

                case 5: // 1.5 기능

                case 6: // 1.6 기능

                case 7: // 1.7 기능

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