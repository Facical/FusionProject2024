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

                // 1.1 선발 일정 조회 기능
                case 1:
                    // 서버에 일정 조회 요청 메시지를 생성하고 패킷으로 변환하여 전송
                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.CHECK_SCHEDULE, Packet.NOT_USED, "일정 조회 요청");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버로부터 응답 메시지를 수신하여 처리
                    rxMsg = Message.readMessage(in);

                    // 응답 메시지가 결과 타입인 경우
                    if (rxMsg.getType() == Packet.RESULT) {
                        if (rxMsg.getDetail() == Packet.SUCCESS) {
                            // 일정 데이터가 성공적으로 수신된 경우, 데이터를 파싱하여 각 일정을 출력
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
                            // 조회 실패 시, 서버에서 전달된 실패 메시지를 출력
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

                // 1.5 결핵진단서 제출 기능
                case 5:
                    // 결핵진단서 제출 가능 기간인지 체크하는 패킷 전송
                    out.write(Packet.makePacket(Message.makeMessage(Packet.REQUEST, Packet.CHECK_DATE, Packet.SUBMIT_CERTIFICATE, "")));
                    out.flush();

                    // 서버로부터 응답 수신
                    rxMsg = Message.readMessage(in);
                    if(rxMsg.getDetail() == Packet.FAIL){
                        System.out.println("결핵진단서 제출 기간이 아닙니다!");
                        break;
                    }

                    // 파일 경로 입력 받기
                    System.out.println("=== 결핵진단서 제출 ===");
                    System.out.print("제출할 파일 경로 입력: ");
                    String filePath = br.readLine();

                    // 파일 존재 여부 및 읽기 권한 체크
                    File file = new File(filePath);
                    if (!file.exists() || !file.canRead()) {
                        System.out.println("파일이 존재하지 않습니다.");
                        break;
                    }

                    // 파일명과 확장자 추출
                    String fileName = file.getName();
                    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

                    // 허용된 파일 형식인지 검사 (jpg, jpeg, png만 허용)
                    if (!fileType.equals("jpg") && !fileType.equals("jpeg") && !fileType.equals("png")) {
                        System.out.println("지원되지 않는 파일 형식입니다. (jpg, jpeg, png만 가능)");
                        break;
                    }

                    // 파일을 바이트 배열로 읽기
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    if (fileData == null || fileData.length == 0) {
                        System.out.println("파일 데이터가 비어 있습니다.");
                        break;
                    }

                    // 파일 크기 제한 검사 (10MB)
                    if (fileData.length > 10 * 1024 * 1024) {
                        System.out.println("파일 크기가 너무 큽니다. (최대 10MB)");
                        break;
                    }

                    // 파일 데이터를 Base64로 인코딩
                    String encodedData = Base64.getEncoder().encodeToString(fileData);
                    System.out.println("Encoded Data Length: " + encodedData.length()); // 디버깅용 로그

                    // 전송할 데이터 포맷 생성 (학번,인코딩된데이터,파일명,파일타입)
                    String certificateData = String.format("%d,%s,%s,%s",
                            studentId,
                            encodedData,
                            fileName,
                            fileType);

                    // 결핵진단서 제출 요청 패킷 생성 및 전송
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.SUBMIT_CERTIFICATE,
                            Packet.NOT_USED,
                            certificateData);
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버로부터 제출 결과 수신
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

                // 2.1 선발 일정 등록 기능
                case 1:
                    // 선발 일정 등록 시작 안내 및 사용자 입력 받기
                    System.out.println("=== 선발 일정 등록 ===");
                    System.out.print("기간명 입력: ");
                    String periodName = br.readLine(); // 기간명 입력
                    System.out.print("시작일 입력 (yyyy-MM-dd HH:mm:ss): ");
                    String startDate = br.readLine(); // 시작일 입력
                    System.out.print("종료일 입력 (yyyy-MM-dd HH:mm:ss): ");
                    String endDate = br.readLine(); // 종료일 입력

                    // 입력받은 데이터를 일정 데이터 형식으로 포맷
                    String scheduleData = String.format("%s,%s,%s", periodName, startDate, endDate);

                    // 일정 등록 요청 메시지 생성 및 패킷 전송
                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.REGISTER_SCHEDULE, Packet.NOT_USED, scheduleData);
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버로부터 응답 메시지 수신 및 결과 처리
                    rxMsg = Message.readMessage(in);

                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        // 일정 등록 성공 시 출력
                        System.out.println("일정 등록 성공!");
                    } else {
                        // 일정 등록 실패 시 실패 사유 출력
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


                // 2.7 결핵진단서 제출 현황 조회 및 다운로드 기능
                case 7:
                    System.out.println("=== 결핵진단서 제출 현황 ===");

                    // 서버에 결핵진단서 제출 현황 조회 요청
                    txMsg = Message.makeMessage(Packet.REQUEST,
                            Packet.CHECK_CERTIFICATES,
                            Packet.NOT_USED,
                            "진단서 조회 요청");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버로부터 제출 현황 데이터 수신
                    rxMsg = Message.readMessage(in);

                    if (rxMsg.getType() == Packet.RESULT) {
                        if (rxMsg.getDetail() == Packet.SUCCESS) {
                            // 세미콜론으로 구분된 진단서 정보를 배열로 분리
                            String[] certificates = rxMsg.getData().split(";");
                            if (certificates.length > 0 && !certificates[0].trim().isEmpty()) {
                                // 각 진단서 정보 출력 (학생ID, 제출일)
                                for (String cert : certificates) {
                                    String[] parts = cert.split(",");
                                    System.out.println("학생 ID: " + parts[0]);
                                    System.out.println("제출일: " + parts[1]);
                                    System.out.println("---------------");
                                }

                                // 진단서 다운로드 여부 확인
                                System.out.print("진단서 파일을 다운로드하시겠습니까? (Y/N): ");
                                String answer = br.readLine().trim().toUpperCase();

                                if (answer.equals("Y")) {
                                    // 서버에 다운로드 요청
                                    txMsg = Message.makeMessage(Packet.REQUEST,
                                            Packet.SUBMIT_CERTIFICATE,
                                            Packet.NOT_USED,
                                            "DOWNLOAD");
                                    packet = Packet.makePacket(txMsg);
                                    out.write(packet);
                                    out.flush();

                                    // 서버로부터 파일 데이터 수신
                                    rxMsg = Message.readMessage(in);
                                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                                        // 저장 경로 입력 받기
                                        System.out.print("저장할 디렉토리 경로를 입력하세요: ");
                                        String savePath = br.readLine().trim();

                                        // 서버에서 받은 데이터 파싱 (형식: "studentId,fileName,fileType,base64data;...")
                                        String serverData = rxMsg.getData().trim();
                                        String[] entries = serverData.split(";");

                                        // 저장 디렉토리 생성
                                        File dir = new File(savePath);
                                        if (!dir.exists()) {
                                            dir.mkdirs();
                                        }

                                        // 각 진단서 파일 처리
                                        for (String entry : entries) {
                                            entry = entry.trim();
                                            if (entry.isEmpty()) continue;

                                            // 파일 데이터 파싱
                                            String[] fileParts = entry.split(",");
                                            if (fileParts.length != 4) {
                                                System.err.println("잘못된 데이터 포맷: " + entry);
                                                continue;
                                            }

                                            // 파일 정보 추출
                                            String sid = fileParts[0].trim();
                                            String fName = fileParts[1].trim();
                                            String fType = fileParts[2].trim();
                                            String base64Data = fileParts[3].trim();

                                            // base64 데이터 유효성 검사
                                            if (base64Data.isEmpty()) {
                                                System.err.println("Base64 데이터가 비어있습니다: " + entry);
                                                continue;
                                            }

                                            // Base64 디코딩 및 파일 저장
                                            try {
                                                // Base64 문자열을 바이트 배열로 디코딩
                                                byte[] fileData = Base64.getDecoder().decode(base64Data);
                                                // 파일명 형식: studentId_fileName
                                                File outFile = new File(savePath, sid + "_" + fName);
                                                Files.write(outFile.toPath(), fileData);
                                            } catch (IllegalArgumentException e) {
                                                // Base64 디코딩 실패 시 디버깅 정보 출력
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

                case 8: // 2.8 퇴사 신청자 조회 및 환불 처리 기능
                    System.out.println("=== 퇴사 신청자 조회 및 환불 ===");

                    // 승인된 퇴사자 목록 요청 메시지 생성 및 전송
                    txMsg = Message.makeMessage(Packet.REQUEST, Packet.PROCESS_WITHDRAWAL, Packet.NOT_USED, "approved_withdrawals");
                    packet = Packet.makePacket(txMsg);
                    out.write(packet);
                    out.flush();

                    // 서버로부터 응답 메시지 수신
                    rxMsg = Message.readMessage(in);

                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                        // 수신된 데이터를 줄 단위로 분리
                        String[] lines = rxMsg.getData().split("\n");
                        String currentDorm = null;
                        boolean hasWithdraws = false;

                        for (String line : lines) {
                            String[] parts = line.split("\\|");
                            if (parts[0].equals("DORM_START")) {
                                // 생활관 시작 구간
                                currentDorm = parts[1];
                                System.out.println("\n=== " + currentDorm + " ===");
                            } else if (parts[0].equals("STUDENT")) {
                                // 학생 퇴사 정보 출력
                                hasWithdraws = true;
                                System.out.println("학생명: " + parts[1] + " (학생ID: " + parts[2] + ")");
                                System.out.println("퇴사일: " + parts[3]);
                                System.out.println("은행명: " + parts[4]);
                                System.out.println("계좌번호: " + parts[5]);
                                System.out.println("환불금액: " + parts[6]);
                                System.out.println("---------------");
                            } else if (parts[0].equals("DORM_END")) {
                                // 생활관 종료 구간
                                if (!hasWithdraws && currentDorm != null) {
                                    System.out.println("해당 생활관의 퇴사 신청자가 없습니다.");
                                    hasWithdraws = false;
                                }
                            }
                        }

                        if (hasWithdraws) {
                            // 퇴사자가 있을 경우 환불 처리 여부 확인
                            System.out.print("\n환불 처리를 진행하시겠습니까? (Y/N): ");
                            String answer = br.readLine().trim().toUpperCase();

                            if (answer.equals("Y")) {
                                // 환불 처리 요청 메시지 생성 및 전송
                                txMsg = Message.makeMessage(Packet.REQUEST, Packet.PROCESS_WITHDRAWAL, Packet.SUCCESS, "process_refunds");
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();

                                rxMsg = Message.readMessage(in);
                                if (rxMsg.getDetail() == Packet.SUCCESS) {
                                    // 환불 처리 성공 메시지 출력
                                    System.out.println("환불 처리가 완료되었습니다.");
                                } else {
                                    // 환불 처리 실패 메시지 출력
                                    System.out.println("환불 처리 중 오류가 발생했습니다.");
                                }
                            }
                        } else {
                            // 퇴사 신청자가 없는 경우
                            System.out.println("승인된 퇴사 신청자가 없습니다.");
                        }
                    } else {
                        // 승인된 퇴사자가 없는 경우 메시지 출력
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