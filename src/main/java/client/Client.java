package client;

import dto.ScheduleDTO;
import network.Message;
import common.Packet;
import service.ScheduleService;
import service.StudentService;
import view.StudentViewer;
import view.AdminViewer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
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
    private String gender = null;

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
                            gender = parts[2];

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
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_DATE, Packet.APPLY_ADMISSION, "")));
                    out.flush();

                    rxMsg = Message.readMessage(in);
                    if(rxMsg.getDetail() == Packet.FAIL){
                        System.out.println("생활관 입사 신청 기간이 아닙니다!");
                        break;
                    }


                    System.out.println("============= 입사 신청 =============");
                    System.out.println("오름관 1동, 푸름관 3동 : 여자만 신청 가능");
                    System.out.println();
                    StudentService studentService = new StudentService();
                    String gender = studentService.getGender(loggedInUserId);
                    String firstDormitory = "";
                    String firstDormitoryMeal = "";
                    String secondDormitory = "";
                    String secondDormitoryMeal = "";
                    while (true) {
                        System.out.print("1지망 생활관 입력 : ");
                        firstDormitory = br.readLine();
                        if (gender.equals("M") && (firstDormitory.equals("오름관1동") || firstDormitory.equals("푸름관3동"))) {
                            System.out.println(firstDormitory + "은 여자만 신청 가능합니다!");
                        } else if (gender.equals("F") && !(firstDormitory.equals("오름관1동") || firstDormitory.equals("푸름관3동"))) {
                            System.out.println(firstDormitory + "은 남자만 신청 가능합니다!");
                        } else {
                            break;
                        }
                    }
                    // 1지망 생활관 식사 입력
                    if (firstDormitory.equals("오름관2동") || firstDormitory.equals("오름관3동")) {
                        System.out.print(firstDormitory + "의 식사 입력 (5일식, 7일식) : ");
                    } else {
                        System.out.print(firstDormitory + "의 식사 입력 (5일식, 7일식, 선택안함) : ");
                    }
                    firstDormitoryMeal = br.readLine();
                    //111
                    // 2지망 생활관 입력 로직
                    while (true) {
                        System.out.print("2지망 생활관 입력 : ");
                        secondDormitory = br.readLine();

                        if (gender.equals("M") && (secondDormitory.equals("오름관1동") || secondDormitory.equals("푸름관3동"))) {
                            System.out.println(secondDormitory + "은 여자만 신청 가능합니다!");
                        } else if (gender.equals("F") && !(secondDormitory.equals("오름관1동") || secondDormitory.equals("푸름관3동"))) {
                            System.out.println(secondDormitory + "은 남자만 신청 가능합니다!");
                        } else {
                            break; // 올바른 입력인 경우 루프 탈출
                        }
                    }
                    // 2지망 생활관 식사 입력
                    if (secondDormitory.equals("오름관2동") || secondDormitory.equals("오름관3동")) {
                        System.out.print(secondDormitory + "의 식사 입력 (5일식, 7일식) : ");
                    } else {
                        System.out.print(secondDormitory + "의 식사 입력 (5일식, 7일식, 선택안함) : ");
                    }
                    secondDormitoryMeal = br.readLine();

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

                    if (rxMsg.getType() == Packet.RESULT) {
                        if (rxMsg.getDetail() == Packet.SUCCESS) {
                            System.out.println("입사 신청 성공!");
                        } else {
                            System.out.println("입사 신청 실패");
                        }
                    }

                    break;

                case 3: // 1.3 기능
                    //합격 여부 및 호실 확인
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_DATE, Packet.CHECK_ADMISSION, "")));
                    out.flush();

                    rxMsg = Message.readMessage(in);
                    if(rxMsg.getDetail() == Packet.FAIL){
                        System.out.println("생활관 배정 및 합격자 발표 기간이 아닙니다!");
                        break;
                    }


                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.CHECK_ADMISSION,
                            Packet.NOT_USED, "합격 여부 및 호실 확인 조회 요청");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버에서 DB 조회결과
                    rxMsg = new Message();
                    header = new byte[Packet.LEN_HEADER];
                    in.read(header);
                    Message.makeMessageHeader(rxMsg, header);
                    body = new byte[rxMsg.getLength()];
                    in.read(body);
                    Message.makeMessageBody(rxMsg, body);

                    String data = rxMsg.getData();
                    if (rxMsg.getData() == null)
                        System.out.println("불합격");

                    else
                    {
                        System.out.println(data);
                    }

                    break;


                case 4: // 1.4 생활관 비용 확인 및 납부

                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_DATE, Packet.CHECK_PAY_DORMITORY, "")));
                    out.flush();

                    rxMsg = Message.readMessage(in);
                    if(rxMsg.getDetail() == Packet.FAIL){
                        System.out.println("생활관 비용 확인 및 납부 기간이 아닙니다!");
                        break;
                    }

                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_PAY_DORMITORY, Packet.NOT_USED, "")));
                    out.flush();

                    rxMsg = Message.readMessage(in);
                    Message.printMessage(rxMsg);

                    byte type = rxMsg.getType();
                    byte detail = rxMsg.getDetail();
                    if (type == Packet.RESPONSE && detail == Packet.SUCCESS) {
                        data = rxMsg.getData();
                        if (data != null && !data.isEmpty()) {
                            String[] parts = data.split(",");
                            String roomFee = parts[0];
                            String mealFee = parts[1];
                            String totalFee = parts[2];
                            String paymantStatus = parts[3];
                            System.out.println("기숙사 비용: " + roomFee + "원");
                            System.out.println("식사 비용: " + mealFee + "원");
                            System.out.println("총 비용(기숙사 비용 + 식사 비용): " + totalFee + "원");
                            System.out.println("납부 상태: " + paymantStatus);
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
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_DATE, Packet.SUBMIT_CERTIFICATE, "")));
                    out.flush();

                    rxMsg = Message.readMessage(in);
                    if(rxMsg.getDetail() == Packet.FAIL){
                        System.out.println("결핵진단서 제출 기간이 아닙니다!");
                        break;
                    }

                    System.out.println("=== 결핵진단서 제출 ===");
                    System.out.print("제출할 파일 경로 입력: ");
                    String filePath = br.readLine();

                    File file = new File(filePath);
                    if (!file.exists() || !file.canRead()) {
                        System.out.println("파일이 존재하지 않습니다.");
                        break;
                    }

                    String fileName = file.getName();
                    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

                    if (!fileType.equals("jpg") && !fileType.equals("jpeg") && !fileType.equals("png")) {
                        System.out.println("지원되지 않는 파일 형식입니다. (jpg, jpeg, png만 가능)");
                        break;
                    }

                    byte[] fileData = Files.readAllBytes(file.toPath());
                    if (fileData == null || fileData.length == 0) {
                        System.out.println("파일 데이터가 비어 있습니다.");
                        break; // 비어 있는 데이터 처리 종료
                    }

                    // 파일 크기 제한 추가 (10MB)
                    if (fileData.length > 10 * 1024 * 1024) {
                        System.out.println("파일 크기가 너무 큽니다. (최대 10MB)");
                        break;
                    }

                    String encodedData = Base64.getEncoder().encodeToString(fileData);
                    System.out.println("Encoded Data Length: " + encodedData.length()); // 디버깅용 로그



                    String certificateData = String.format("%d,%s,%s,%s",
                            studentId,
                            encodedData,
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
                    String bankName = br.readLine();
                    System.out.print("계좌 번호: ");
                    String accountNumber = br.readLine();
                    System.out.print("퇴사 신청 사유: ");
                    String reason = br.readLine();
                    System.out.print("퇴사 날짜(ex: 20240101): ");
                    String quitDate = br.readLine();

                    newData = bankName + "," + accountNumber + "," + reason + "," + quitDate;
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
                    System.out.print("생활관 입력 : ");
                    String dormitoryName = br.readLine();
                    System.out.print("생활관 사용료 입력 : ");
                    String dormitoryUsageFee = br.readLine();
                    System.out.println("생활관 급식비 등록");
                    System.out.print(dormitoryName + "의 5일식 급식비를 입력하세요 : ");
                    String fiveMealFee = br.readLine();
                    System.out.print(dormitoryName + "의 7일식 급식비를 입력하세요 : ");
                    String sevenMealFee = br.readLine();
                    String newData = dormitoryName + "," + dormitoryUsageFee + "," + fiveMealFee + "," + sevenMealFee;
                    // 오름관2동이나 3동이면 "선택안함" 항목의 급식비 0을 포함
                    if (dormitoryName.equals("오름관2동") || dormitoryName.equals("오름관3동")) {
                        newData += ",0"; // "선택안함" 급식비 0 추가
                    }
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
                    System.out.println("=== 입사 신청자 조회 ===");
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.VIEW_APPLICANTS,
                            Packet.NOT_USED,
                            "");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버가 보내준 패킷 받아서 해석
                    rxMsg = new Message();
                    header = new byte[Packet.LEN_HEADER];
                    in.read(header);
                    Message.makeMessageHeader(rxMsg, header);
                    body = new byte[rxMsg.getLength()];
                    in.read(body);
                    Message.makeMessageBody(rxMsg, body);
                    System.out.println(rxMsg.getData());

                    break;

                case 4: // 2.4 입사자 선발 및 호실 배정 기능
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.SELECT_STUDENTS,
                            Packet.NOT_USED,
                            "");
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

                    System.out.println(rxMsg.getData());

                    break;

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
                    rxMsg = new Message();
                    header = new byte[Packet.LEN_HEADER];
                    in.read(header);
                    Message.makeMessageHeader(rxMsg, header);
                    body = new byte[rxMsg.getLength()];
                    in.read(body);
                    Message.makeMessageBody(rxMsg, body);

                    System.out.println(rxMsg.getData());
                    break;

                case 6: // 2.6 생활관 비용 미납부자 조회 기능
                    System.out.println("=== 생활관 비용 미납부자 조회 ===");
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.VIEW_UNPAID_STUDENTS,
                            Packet.NOT_USED,
                            "");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();
                    // 위에 까지가 조회 요청

                    // 조회 요청해서 서버가 DB 조회해서 뿌려주는 것
                    rxMsg = new Message();
                    header = new byte[Packet.LEN_HEADER];
                    in.read(header);
                    Message.makeMessageHeader(rxMsg, header);
                    body = new byte[rxMsg.getLength()];
                    in.read(body);
                    Message.makeMessageBody(rxMsg, body);

                    System.out.println(rxMsg.getData());
                    break;

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
                            if (certificates.length > 0 && !certificates[0].trim().isEmpty()) {
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
                                    // 단순히 "DOWNLOAD" 요청을 보내서 서버가 Base64 인코딩된 파일 리스트를 보내도록 수정 필요
                                    txMsg = Message.makeMessage(Packet.REQUEST,
                                            Packet.SUBMIT_CERTIFICATE,
                                            Packet.NOT_USED,
                                            "DOWNLOAD");
                                    packet = Packet.makePacket(txMsg);
                                    out.write(packet);
                                    out.flush();

                                    rxMsg = Message.readMessage(in);
                                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                                        System.out.print("저장할 디렉토리 경로를 입력하세요: ");
                                        String savePath = br.readLine().trim();

                                        // 서버에서 받은 데이터: "studentId,fileName,fileType,base64data;..."
                                        String serverData = rxMsg.getData().trim();
                                        String[] entries = serverData.split(";");

                                        // 디렉토리 존재 여부 확인 및 생성
                                        File dir = new File(savePath);
                                        if (!dir.exists()) {
                                            dir.mkdirs();
                                        }

                                        for (String entry : entries) {
                                            entry = entry.trim();
                                            if (entry.isEmpty()) continue;

                                            String[] fileParts = entry.split(",");
                                            if (fileParts.length != 4) {
                                                System.err.println("잘못된 데이터 포맷: " + entry);
                                                continue;
                                            }

                                            String sid = fileParts[0].trim();
                                            String fName = fileParts[1].trim();
                                            String fType = fileParts[2].trim();
                                            String base64Data = fileParts[3].trim();

                                            if (base64Data.isEmpty()) {
                                                System.err.println("Base64 데이터가 비어있습니다: " + entry);
                                                continue;
                                            }


                                            // 여기서 실제 파일 쓰기
                                            // 클라이언트 로컬 디스크에 파일 저장
                                            try {
                                                byte[] fileData = Base64.getDecoder().decode(base64Data);
                                                // fileData를 로컬에 저장
                                                File outFile = new File(savePath, sid + "_" + fName);
                                                Files.write(outFile.toPath(), fileData);
                                            } catch (IllegalArgumentException e) {
                                                // base64 디코딩 오류 발생 시, 어떤 문자열인지 출력해서 디버깅
                                                System.err.println("Base64 디코딩 오류 발생: " + base64Data);
                                                e.printStackTrace();
                                            }
                                        }
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

                case 8: // 퇴사 신청자 조회 및 환불
                    System.out.println("=== 퇴사 신청자 조회 및 환불 ===");

                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.PROCESS_WITHDRAWAL,
                            Packet.NOT_USED,
                            "approved_withdrawals");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    rxMsg = Message.readMessage(in);

                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        String[] lines = rxMsg.getData().split("\n");
                        String currentDorm = null;
                        boolean hasWithdraws = false;

                        for (String line : lines) {
                            String[] parts = line.split("\\|");
                            if (parts[0].equals("DORM_START")) {
                                currentDorm = parts[1];
                                System.out.println("\n=== " + currentDorm + " ===");
                            }
                            else if (parts[0].equals("STUDENT")) {
                                hasWithdraws = true;
                                System.out.println("학생명: " + parts[1] + " (학생ID: " + parts[2] + ")");
                                System.out.println("퇴사일: " + parts[3]);
                                System.out.println("은행명: " + parts[4]);
                                System.out.println("계좌번호: " + parts[5]);
                                System.out.println("환불금액: " + parts[6]);
                                System.out.println("---------------");
                            }
                            else if (parts[0].equals("DORM_END")) {
                                if (!hasWithdraws && currentDorm != null) {
                                    System.out.println("해당 생활관의 퇴사 신청자가 없습니다.");
                                    hasWithdraws = false;
                                }

                            }
                        }
                        if (hasWithdraws) {
                            System.out.print("\n환불 처리를 진행하시겠습니까? (Y/N): ");
                            String answer = br.readLine().trim().toUpperCase();

                            if (answer.equals("Y")) {
                                txMsg = Message.makeMessage(Packet.REQUEST,
                                        Packet.PROCESS_WITHDRAWAL,
                                        Packet.SUCCESS,
                                        "process_refunds");
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();

                                rxMsg = Message.readMessage(in);
                                if (rxMsg.getDetail() == Packet.SUCCESS) {
                                    System.out.println("환불 처리가 완료되었습니다.");
                                } else {
                                    System.out.println("환불 처리 중 오류가 발생했습니다.");
                                }
                            }
                        } else {
                            //System.out.println("\n퇴사 신청자가 없습니다.");
                        }
                    } else {
                        System.out.println("승인된 퇴사 신청자가 없습니다.");
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