package client;

import network.Message;
import common.Packet;
import view.StudentViewer;
import view.AdminViewer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Scanner;

public class Client {
    private final String ip;
    private final String port;
    private Socket cliSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private BufferedReader br;
    Scanner sc = new Scanner(System.in);
    Message txMsg = null;
    Message rxMsg = null;
    private static int loggedInUserId = -1;
    private int studentId;


    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;

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
            int id = -1;
            while (true) {
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
                            continue;
                        }

                        id = Integer.parseInt(idInput);

                        System.out.print("비밀번호를 입력하세요: ");
                        String password = br.readLine();

                        String newData = id + "," + password;
                        txMsg = Message.makeMessage(Packet.RESPONSE, Packet.LOGIN,
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
                            int userId = Integer.parseInt(parts[0]);
                            String userRole = parts[1];

                            if (userRole.equals("학생")) {
                                this.studentId = userId;
                                loggedInUserId = userId;
                                studentRun();
                                return;
                            } else if (userRole.equals("관리자")) {
                                adminRun();
                                return;
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void studentRun() throws IOException {
        while (true) {
            int studentMenu = StudentViewer.viewStudentPage();
            switch(studentMenu) {
                case 1:
                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.CHECK_SCHEDULE,
                            Packet.NOT_USED, "일정 조회 요청");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    rxMsg = Message.readMessage(in);

                    if (rxMsg.getType() == Packet.RESULT) {
                        if (rxMsg.getDetail() == Packet.SUCCESS) {
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


                case 4: //생활관 비용 확인 및 납부
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_PAY_DORMITORY, Packet.NOT_USED, "")));
                    out.flush();

                    Message rxMsg = Message.readMessage(in);
                    Message.printMessage(rxMsg);

                    byte type = rxMsg.getType();
                    byte detail = rxMsg.getDetail();
                    if (type == Packet.RESPONSE && detail == Packet.SUCCESS) {
                        data = rxMsg.getData();
                        if (data != null && !data.isEmpty()) {
                            parts = data.split(",");
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
                    break;

                case 5:
                    System.out.println("=== 결핵진단서 제출 ===");
                    System.out.print("제출할 파일 경로 입력: ");
                    String filePath = br.readLine();

                    File file = new File(filePath);
                    if (!file.exists()) {
                        System.out.println("파일이 존재하지 않습니다.");
                        break;
                    }

                    byte[] fileData = Files.readAllBytes(file.toPath());
                    String fileName = file.getName();
                    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);

                    String certificateData = String.format("%d,%s,%s,%s",
                            studentId,
                            Base64.getEncoder().encodeToString(fileData),
                            fileName,
                            fileType);

                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.SUBMIT_CERTIFICATE,
                            Packet.NOT_USED,
                            certificateData);
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    rxMsg = Message.readMessage(in);
                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        System.out.println("결핵진단서 제출 성공");
                    } else {
                        System.out.println("결핵진단서 제출 실패: " + rxMsg.getData());
                    }
                    break;
                case 6:
                    System.out.println("퇴사 신청 메뉴 입니다.");
                    System.out.println("환불 받으실 은행 이름, 계좌 번호, 퇴사 신청 사유를 입력해주세요.");
                    System.out.print("환불 받으실 은행 이름: ");
                    //sc.nextLine();
                    String bankName = sc.nextLine();
                    System.out.print("계좌 번호: ");
                    String accountNumber = sc.nextLine();
                    System.out.print("퇴사 신청 사유: ");
                    String reason = sc.nextLine();

                    newData = bankName + "," + accountNumber + "," + reason;
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.REQUEST_WITHDRAWAL, Packet.NOT_USED, newData)));
                    out.flush();

                    rxMsg = Message.readMessage(in);
                    Message.printMessage(rxMsg);

                    if (rxMsg.getDetail() == Packet.SUCCESS){
                        System.out.println("퇴사 신청이 정상적으로 처리 되었습니다.");
                    }else{
                        System.out.println(rxMsg.getData());
                        System.out.println("퇴사 신청에 실패하였습니다.");
                    }
                break;
                case 7: //환불 결과 조회
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_REFUND, Packet.NOT_USED, "")));
                    out.flush();

                    rxMsg = Message.readMessage(in);
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
                break;
                case 0:
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

    private void adminRun() throws IOException {
        while (true) {
            int adminMenu = AdminViewer.viewAdminPage();
            switch(adminMenu) {
                case 1:
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

                    rxMsg = Message.readMessage(in);

                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        System.out.println("일정 등록 성공!");
                    } else {
                        System.out.println("일정 등록 실패: " + rxMsg.getData());
                    }
                    break;
                case 2: // 2.2 기능
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

                case 3: // 2.3 기능

                case 4: // 2.4 기능

                case 5: // 2.5 기능
                    System.out.println("=== 생활관 비용 납부자 조회 ===");
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.VIEW_PAID_STUDENTS,
                            Packet.NOT_USED,
                            "");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();
                    // 위에 까지가 조회 요청

                    // 조회 요청해서 서버가 DB 조회해서 뿌려주는 것
                    rxMsg = Message.readMessage(in);

                case 6: // 2.6 기능
                    System.out.println("=== 생활관 비용 미납부자 조회 ===");

                case 7:
                    System.out.println("=== 결핵진단서 제출 현황 ===");

                    // 먼저 제출 현황 조회
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.CHECK_CERTIFICATES,
                            Packet.NOT_USED,
                            "진단서 조회 요청");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    rxMsg = Message.readMessage(in);

                    if (rxMsg.getType() == Packet.RESULT) {
                        if (rxMsg.getDetail() == Packet.SUCCESS) {
                            String[] certificates = rxMsg.getData().split(";");
                            if (certificates.length > 0) {
                                for (String cert : certificates) {
                                    String[] parts = cert.split(",");
                                    System.out.println("학생 ID: " + parts[0]);
                                    System.out.println("제출일: " + parts[1]);
                                    System.out.println("---------------");
                                }

                                // 다운로드 여부 확인
                                System.out.print("진단서 파일을 다운로드하시겠습니까? (Y/N): ");
                                String answer = br.readLine().trim().toUpperCase();

                                if (answer.equals("Y")) {
                                    System.out.print("저장할 디렉토리 경로를 입력하세요: ");
                                    String savePath = br.readLine().trim();

                                    // 다운로드 요청
                                    String downloadData = "DOWNLOAD," + savePath;
                                    txMsg = Message.makeMessage(Packet.REQUEST,
                                            Packet.SUBMIT_CERTIFICATE,
                                            Packet.NOT_USED,
                                            downloadData);
                                    packet = Packet.makePacket(txMsg);
                                    out.write(packet);
                                    out.flush();

                                    rxMsg = Message.readMessage(in);
                                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                                        System.out.println("모든 진단서가 다운로드되었습니다.");
                                    } else {
                                        System.out.println("다운로드 실패: " + rxMsg.getData());
                                    }
                                }
                            } else {
                                System.out.println("제출된 결핵진단서가 없습니다.");
                            }
                        } else {
                            System.out.println("조회 실패: " + rxMsg.getData());
                        }
                    }
                    break;

                case 0:
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