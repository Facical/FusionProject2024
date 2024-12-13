package network;

import dao.*;
import dto.*;
import service.*;
import common.Packet;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class Threads extends Thread {
    private Socket socket;
    private UserDTO userDTO;
    private UserDAO userDAO;

    private ScheduleDAO scheduleDAO;
    private ScheduleDTO scheduleDTO;

    private WithdrawDAO withdrawDAO;
    private AdmissionDAO admissionDAO;
    private ApplicationPreferenceDAO applicationPreferenceDAO;
    private ApplicationDAO applicationDAO;
    private RoomDAO roomDAO;
    private MealDAO mealDAO;
    private ApplicantInfoDAO applicantInfoDAO;
    private ApplicantInfoDTO applicantInfoDTO;

    private ScheduleService scheduleService;
    private RoomService roomService;
    private MealService mealService;
    private ApplicationService applicationService;
    private ApplicationPreferenceService applicationPreferenceService;
    private StudentService studentService;
    private AdmissionService admissionService;
    private TuberculosisService tuberculosisService;

    private StudentPaymentDAO studentPaymentDAO;
    private StudentPaymentDTO studentPaymentDTO;

    private DataInputStream in;
    private DataOutputStream out;
    private int studentID;
    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;
    Message txMsg = null;
    private StudentDAO studentDAO;
    Message rxMsg = null;
    private static int loggedInUserId = -1;
    public Threads(Socket socket) {
        this.studentID = 0;
        this.socket = socket;
        this.userDAO = new UserDAO();
        this.scheduleService = new ScheduleService();
        this.tuberculosisService = new TuberculosisService();
        this.roomService = new RoomService();
        this.mealService = new MealService();
        this.scheduleDAO = new ScheduleDAO();
        this.withdrawDAO = new WithdrawDAO();
        this.studentDAO = new StudentDAO();
        this.admissionDAO = new AdmissionDAO();
        this.applicationPreferenceDAO = new ApplicationPreferenceDAO();
        this.applicationDAO = new ApplicationDAO();
        this.roomDAO = new RoomDAO();
        this.mealDAO = new MealDAO();
        this.applicationService = new ApplicationService();
        this.applicationPreferenceService = new ApplicationPreferenceService();
        this.studentService = new StudentService();
        this.admissionService = new AdmissionService();
        this.studentPaymentDAO = new StudentPaymentDAO();
        this.studentPaymentDTO = new StudentPaymentDTO();
        this.applicantInfoDAO = new ApplicantInfoDAO();
        this.applicantInfoDTO = new ApplicantInfoDTO();
    }

    public void run() {
        try {
            //int studentID = 0;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //나중에 지우기
            txMsg = Message.makeMessage(Packet.REQUEST, Packet.LOGIN, Packet.NOT_USED, "Login Request");
            packet = Packet.makePacket(txMsg);
            out.write(packet);
            out.flush();


            while(true) {

                Message rxMsg = Message.readMessage(in);
                Message.printMessage(rxMsg);

                byte type = rxMsg.getType();

                switch(type) {
                    case Packet.REQUEST:
                        byte code = rxMsg.getCode();

                        switch(code) {

                            // 1.1 선발 일정 조회 기능
                            case Packet.CHECK_SCHEDULE:
                                // ScheduleService를 통해 저장된 일정 리스트 가져오기
                                List<ScheduleDTO> schedules = scheduleService.getSchedules();

                                if (!schedules.isEmpty()) {
                                    // 일정이 존재할 경우 데이터를 포맷하여 응답 메시지 생성
                                    String scheduleData = scheduleService.formatScheduleData(schedules);
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.SUCCESS, scheduleData);
                                } else {
                                    // 일정이 없을 경우 실패 메시지 생성
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.FAIL, "일정이 없습니다.");
                                }

                                // 메시지를 패킷으로 변환하여 클라이언트로 전송
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            //학생 기능 2번 (code 2) : 입사신청
                            case Packet.APPLY_ADMISSION:
                                String admissionData = rxMsg.getData();
                                String[] admissionParts = admissionData.split(",");
                                //firstDormitory + "," + firstDormitoryMeal + "," + secondDormitory + "," + secondDormitoryMeal;
                                ApplicationDTO applicationDTO = new ApplicationDTO();
                                ApplicationPreferenceDTO applicationPreferenceDTO = new ApplicationPreferenceDTO();

                                applicationDTO.setStudentId(loggedInUserId);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                applicationDTO.setApplicationDate(LocalDate.now().format(formatter));
                                boolean applicationSuccess = applicationService.applyAdmission(applicationDTO);

                                int applicationId = applicationService.findApplicationId(loggedInUserId);
                                applicationPreferenceDTO.setApplication_id(applicationId);
                                applicationPreferenceDTO.setDormitory_id(mapDormitoryToId(admissionParts[0]));
                                applicationPreferenceDTO.setPreference_order(1); // 1지망


                                applicationPreferenceDTO.setMeal_id(mapMealId(admissionParts[0], admissionParts[1]));


                                boolean preferenceSuccess1 = applicationPreferenceService.applyPreference(applicationPreferenceDTO);


                                applicationPreferenceDTO.setDormitory_id(mapDormitoryToId(admissionParts[2]));
                                applicationPreferenceDTO.setPreference_order(2); // 2지망


                                applicationPreferenceDTO.setMeal_id(mapMealId(admissionParts[2], admissionParts[3]));

                                boolean preferenceSuccess2 = applicationPreferenceService.applyPreference(applicationPreferenceDTO);


                                if (applicationSuccess && preferenceSuccess1 && preferenceSuccess2) {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.APPLY_ADMISSION,
                                            Packet.SUCCESS,
                                            "입사 신청이 완료되었습니다.");
                                    System.out.println("Admission apply successfully: ");
                                } else {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.APPLY_ADMISSION,
                                            Packet.FAIL,
                                            "입사 신청에 실패했습니다.");
                                    System.out.println("Admission apply failed");
                                }
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            //학생 기능 3번 (code 3) : 합격 여부 및 호실 확인
                            case Packet.CHECK_ADMISSION:
                                // 서비스 호출
                                StudentPassCheckService studentPassCheckService = new StudentPassCheckService();
                                StudentPassDTO studentPassDTO = studentPassCheckService.getStudentPassInfo(studentID);

                                // 결과 포맷
                                String result;
                                if (studentPassDTO == null) {
                                    // result = "합격 정보가 없습니다.";
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_ADMISSION, Packet.FAIL, null);
                                } else {
                                    result = studentPassCheckService.formatStudentPassInfo(studentPassDTO);
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_ADMISSION, Packet.SUCCESS, result);
                                }

                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            //학생 기능 4번 (code 4) : 생활관 비용 확인 및 납부
                            case Packet.CHECK_PAY_DORMITORY:
                                applicationDTO = applicationDAO.getApplicationInfo(studentID);
                                AdmissionDTO admissionDTO = admissionDAO.findAdmission(studentID);
                                //ApplicationPreferenceDTO applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId());

                                // ApplicationPreferenceDTO applicationPreferenceDTO;
                                if (admissionDTO == null) {
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.CHECK_PAY_DORMITORY, Packet.FAIL, "합격 대상자가 아닙니다.")));
                                    out.flush();
                                } else {
                                    applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId(),admissionDTO.getDormitoryId());

                                    RoomDTO roomDTO = roomDAO.getRoomInfo(admissionDTO.getRoomId());
                                    MealDTO mealDTO = mealDAO.getMealInfo(applicationPreferenceDTO.getMeal_id());
                                    int totalFee = roomDTO.getFee() + mealDTO.getFee();

                                    String data = roomDTO.getFee() + "," + mealDTO.getFee() + "," + totalFee + "," + admissionDTO.getPaymentStatus();

                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESPONSE, Packet.CHECK_PAY_DORMITORY, Packet.SUCCESS, data)));
                                    out.flush();

                                    rxMsg = Message.readMessage(in);
                                    Message.printMessage(rxMsg);


                                    if (rxMsg.getDetail() == Packet.SUCCESS) {
                                        admissionDTO.setPaymentStatus("납부 완료");
                                        admissionDAO.UpdatePaymentStatus(admissionDTO);
                                    }


                                    admissionDTO = admissionDAO.findAdmission(studentID);
                                    if (admissionDTO.getPaymentStatus().equals("납부 완료")) {
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.SUCCESS, "");
                                    } else if (admissionDTO.getPaymentStatus().equals("미납")) {
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.FAIL, "");
                                    }

                                    packet = Packet.makePacket(txMsg);
                                    out.write(packet);
                                    out.flush();
                                }
                                break;

                            //1.5 결핵진단서 제출 기능
                            case Packet.SUBMIT_CERTIFICATE:
                                // 수신된 데이터를 콤마로 구분하여 분리
                                String[] certParts = rxMsg.getData().split(",");

                                // 다운로드 요청인 경우 처리
                                if (certParts[0].equals("DOWNLOAD")) {
                                    // 모든 결핵진단서 정보를 데이터베이스에서 조회
                                    List<TuberculosisDTO> allCertificates = tuberculosisService.getCertificates();
                                    if (allCertificates == null || allCertificates.isEmpty()) {
                                        // 제출된 진단서가 없는 경우
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.SUBMIT_CERTIFICATE,
                                                Packet.FAIL,
                                                "No certificates found.");
                                    } else {
                                        // 조회된 진단서들을 Base64로 인코딩하여 전송 데이터 생성
                                        // 형식: "studentId,fileName,fileType,base64data;"
                                        StringBuilder sb = new StringBuilder();
                                        for (TuberculosisDTO certDTO : allCertificates) {
                                            // 이미지 데이터가 있는 경우만 처리
                                            if (certDTO.getImageData() == null) continue;

                                            // 이미지 데이터를 Base64로 인코딩
                                            String base64Data = Base64.getEncoder().encodeToString(certDTO.getImageData());

                                            // 진단서 정보를 문자열로 구성
                                            sb.append(certDTO.getStudentId()).append(",")
                                                    .append(certDTO.getFileName()).append(",")
                                                    .append(certDTO.getFileType()).append(",")
                                                    .append(base64Data).append(";");
                                        }

                                        // 마지막 세미콜론 제거
                                        if (sb.length() > 0) {
                                            sb.deleteCharAt(sb.length() - 1);
                                        }

                                        String sendData = sb.toString();
                                        if (sendData.isEmpty()) {
                                            // 유효한 진단서가 없는 경우
                                            txMsg = Message.makeMessage(Packet.RESULT,
                                                    Packet.SUBMIT_CERTIFICATE,
                                                    Packet.FAIL,
                                                    "No valid certificates to download.");
                                        } else {
                                            // 전송 데이터 생성
                                            txMsg = Message.makeMessage(Packet.RESULT,
                                                    Packet.SUBMIT_CERTIFICATE,
                                                    Packet.SUCCESS,
                                                    sendData);
                                        }
                                    }

                                    // 응답 전송
                                    packet = Packet.makePacket(txMsg);
                                    out.write(packet);
                                    out.flush();
                                }
                                // 진단서 제출 요청 처리
                                else {
                                    // 클라이언트에서 전송한 데이터 파싱
                                    int studentId = Integer.parseInt(certParts[0]);
                                    byte[] fileData = Base64.getDecoder().decode(certParts[1]);
                                    String fileName = certParts[2];
                                    String fileType = certParts[3];

                                    // 진단서 DTO 객체 생성
                                    TuberculosisDTO cert = new TuberculosisDTO();
                                    cert.setStudentId(studentId);
                                    cert.setImageData(fileData);
                                    cert.setFileName(fileName);
                                    cert.setFileType(fileType);

                                    // 진단서 제출 처리
                                    String submitResult = tuberculosisService.submitCertificate(cert);

                                    try {
                                        // Base64 디코딩 검증
                                        fileData = Base64.getDecoder().decode(certParts[1]);
                                        if (fileData == null || fileData.length == 0) {
                                            throw new IllegalArgumentException("디코딩된 파일 데이터가 비어 있습니다.");
                                        }
                                    } catch (IllegalArgumentException e) {
                                        // Base64 디코딩 실패 처리
                                        System.err.println("Base64 디코딩 실패: " + e.getMessage());
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.SUBMIT_CERTIFICATE,
                                                Packet.FAIL,
                                                "잘못된 Base64 데이터입니다.");
                                        break;
                                    }

                                    // 제출 결과에 따른 응답 메시지 생성
                                    if (submitResult.equals("성공")) {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.SUBMIT_CERTIFICATE,
                                                Packet.SUCCESS,
                                                "결핵진단서 제출 및 상태 업데이트 완료");
                                    } else {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.SUBMIT_CERTIFICATE,
                                                Packet.FAIL,
                                                submitResult);
                                    }

                                    // 응답 전송
                                    packet = Packet.makePacket(txMsg);
                                    out.write(packet);
                                    out.flush();
                                }
                                break;

                            //학생 기능 6번 (code 6) : 퇴사 신청
                            case Packet.REQUEST_WITHDRAWAL:
                                LocalDate now = LocalDate.now();
                                applicationDTO = applicationDAO.getApplicationInfo(studentID);
                                admissionDTO = admissionDAO.findAdmission(studentID);

                                if(admissionDTO == null){
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.REQUEST_WITHDRAWAL, Packet.FAIL, "퇴사 신청 대상자가 아닙니다.")));
                                    out.flush();
                                }else {
                                    applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId(), admissionDTO.getDormitoryId());

                                    WithdrawDTO withdraw = new WithdrawDTO();
                                    String data = rxMsg.getData();

                                    String[] parts = data.split(",");
                                    String bankName = parts[0];
                                    String accountNumber = parts[1];
                                    String reason = parts[2];
                                    String quitDate = parts[3];

                                    withdraw.setStudentId(studentID);
                                    withdraw.setApplicationDate(now.toString());

                                    //퇴사 상태
                                    RoomDTO roomDTO = roomDAO.getRoomInfo(admissionDTO.getRoomId());
                                    MealDTO mealDTO = mealDAO.getMealInfo(applicationPreferenceDTO.getMeal_id());
                                    int totalFee = roomDTO.getFee() + mealDTO.getFee();

                                    if(now.isBefore(admissionDTO.getResidenceStartDate())){
                                        withdraw.setWithdrawalType("입사 전");
                                        //환불 금액
                                        withdraw.setRefundAmount(totalFee);
                                    }
                                    else{
                                        DateTimeFormatter quitFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                                        LocalDate quit = LocalDate.parse(quitDate, quitFormatter);

                                        Period remainPeriod = Period.between(quit, admissionDTO.getResidenceEndDate());
                                        Period totalPeriod = Period.between(admissionDTO.getResidenceStartDate(), admissionDTO.getResidenceEndDate());
                                        withdraw.setWithdrawalType("입사 후");

                                        //환불 금액
                                        int date = remainPeriod.getYears() * 365 + remainPeriod.getMonths() * 30 + remainPeriod.getDays();
                                        int totalDate = totalPeriod.getYears() * 365 + totalPeriod.getMonths() * 30 + totalPeriod.getDays();
                                        int fee = (totalFee / totalDate) * date;
                                        withdraw.setRefundAmount(fee);
                                    }


                                    withdraw.setWithdrawalDate(quitDate);
                                    withdraw.setBankName(bankName);
                                    withdraw.setAccountNumber(parseInt(accountNumber));

                                    withdraw.setWithdrawalStatus("승인");
                                    withdraw.setReason(reason);
                                    //생활관 아이디
                                    withdraw.setDormitoryId(applicationPreferenceDTO.getDormitory_id());

                                    admissionDTO.setAdmissionStatus("입사 취소");
                                    admissionDAO.UpdateAdmissionStatus(admissionDTO);
                                    withdrawDAO.setWithdrawInfo(withdraw);

                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.REQUEST_WITHDRAWAL, Packet.SUCCESS, "")));
                                    out.flush();
                                }
                                break;

                            //학생 기능 7번 (code 7) : 환불 확인
                            case Packet.CHECK_REFUND:
                                WithdrawDTO withdraw;
                                withdraw = withdrawDAO.getWithdrawInfo(studentID);

                                RefundDTO refundDTO = null;
                                RefundDAO refundDAO = new RefundDAO();
                                refundDTO = refundDAO.getRefundsByWithdrawId(withdraw.getWithdrawalId());


                                if(refundDTO == null){
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.CHECK_REFUND, Packet.FAIL, "환불 처리가 되지 않았거나 퇴사 신청을 하지 않았습니다.")));
                                    out.flush();
                                }else{
                                    String newData;
                                    if(refundDTO.getIsProcessed() == 1){
                                        newData = "취소";
                                    }else {
                                        newData = "승인";
                                    }
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.CHECK_REFUND, Packet.SUCCESS, newData)));
                                    out.flush();
                                }
                                break;

                            // 2.1 선발 일정 등록 기능
                            case Packet.REGISTER_SCHEDULE:
                                // 클라이언트로부터 전송된 일정 데이터 수신
                                String scheduleData = rxMsg.getData();
                                try {
                                    // 수신된 데이터 파싱 (형식: periodName,startDate,endDate)
                                    String[] parts = scheduleData.split(",");
                                    if (parts.length != 3) {
                                        // 데이터 형식이 올바르지 않은 경우 예외 발생
                                        throw new IllegalArgumentException("잘못된 데이터 형식");
                                    }

                                    // 파싱된 데이터를 기반으로 ScheduleDTO 객체 생성 및 설정
                                    ScheduleDTO newSchedule = new ScheduleDTO();
                                    newSchedule.setPeriodName(parts[0]); // 기간명 설정

                                    // 시작일 및 시간 분리
                                    String[] startDateTime = parts[1].split(" ");
                                    String[] endDateTime = parts[2].split(" ");

                                    newSchedule.setStartDate(startDateTime[0]); // 시작 날짜 설정
                                    newSchedule.setStartHour(startDateTime[1]); // 시작 시간 설정
                                    newSchedule.setEndDate(endDateTime[0]); // 종료 날짜 설정
                                    newSchedule.setEndHour(endDateTime[1]); // 종료 시간 설정

                                    // ScheduleService를 통해 일정 등록 처리
                                    boolean success = scheduleService.registerSchedule(newSchedule);

                                    if (success) {
                                        // 일정 등록 성공 시 성공 응답 메시지 생성
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.REGISTER_SCHEDULE, Packet.SUCCESS, "일정 등록이 완료되었습니다.");
                                        System.out.println("Schedule registered successfully: " + newSchedule.getPeriodName());
                                    } else {
                                        // 일정 등록 실패 시 실패 응답 메시지 생성
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.REGISTER_SCHEDULE, Packet.FAIL, "일정 등록에 실패했습니다.");
                                        System.out.println("Schedule registration failed");
                                    }

                                } catch (IllegalArgumentException e) {
                                    // 데이터 형식 오류 처리
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.REGISTER_SCHEDULE, Packet.FAIL, "입력 데이터 형식이 잘못되었습니다.");
                                    System.err.println("Data format error: " + e.getMessage());
                                } catch (Exception e) {
                                    // 예상치 못한 오류 처리
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.REGISTER_SCHEDULE, Packet.FAIL, "일정 등록 중 오류가 발생했습니다.");
                                    System.err.println("Unexpected error: " + e.getMessage());
                                }

                                // 생성된 메시지를 패킷으로 변환하여 클라이언트로 전송
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            // 2.2  생활관 사용료 및 급식비 등록 기능
                            case Packet.REGISTER_FEE:
                                //클라이언트로 부터 요청 메시지 데이터 파싱.
                                String feeData = rxMsg.getData();
                                String[] parts = feeData.split(",");
                                // 필요한 DTO 생성(RoomDTO)
                                RoomDTO roomDTO = new RoomDTO();
                                int dormitory_id = -1;
                                int fee = Integer.parseInt(parts[1]);
                                boolean updateSuccess = false;
                                // 클라이언트가 입력한 생활관 이름을 DB에 저장되어 있는 dormitory_id로 바꾸는 과정.
                                switch (parts[0]) {
                                    case "푸름관1동":
                                        dormitory_id = 1;
                                        roomDTO.setDormitoryId(1);
                                        break;
                                    case "푸름관2동":
                                        dormitory_id = 2;
                                        roomDTO.setDormitoryId(2);
                                        break;
                                    case "푸름관3동":
                                        dormitory_id = 3;
                                        roomDTO.setDormitoryId(3);
                                        break;
                                    case "푸름관4동":
                                        dormitory_id = 4;
                                        roomDTO.setDormitoryId(4);
                                        break;
                                    case "오름관1동":
                                        dormitory_id = 5;
                                        roomDTO.setDormitoryId(5);
                                        break;
                                    case "오름관2동":
                                        dormitory_id = 6;
                                        roomDTO.setDormitoryId(6);
                                        break;
                                    case "오름관3동":
                                        dormitory_id = 7;
                                        roomDTO.setDormitoryId(7);
                                        break;
                                }
                                // DB에 ROOM 테이블에 RoomService 를 통해 생활관과 생활관비 정보를 UPDATE 하는 과정.
                                if (dormitory_id > -1) {
                                    updateSuccess = roomService.updateRoomFeeByDormitoryId(dormitory_id, fee);
                                    if (updateSuccess) {
                                        System.out.println("Fee successfully updated for dormitory ID: " + dormitory_id);
                                    } else {
                                        System.out.println("Failed to update fee for dormitory ID: " + dormitory_id);
                                    }
                                } else {
                                    System.out.println("Invalid dormitory name: " + parts[0]);
                                }
                                int dormitoryId = roomDTO.getDormitoryId();
                                // 7일식 정보 DTO에 매핑.
                                MealDTO mealDTO1 = new MealDTO();
                                mealDTO1.setDormitoryId(dormitoryId);
                                mealDTO1.setName("7일식");
                                mealDTO1.setFee(Integer.parseInt(parts[3]));
                                // 5일식 정보 DTO에 매핑.
                                MealDTO mealDTO2 = new MealDTO();
                                mealDTO2.setDormitoryId(dormitoryId);
                                mealDTO2.setName("5일식");
                                mealDTO2.setFee(Integer.parseInt(parts[2]));
                                // Meal 서비스를 통해 DB에 MEAL 테이블에 INSERT하는 과정.
                                boolean mealSuccess1 = mealService.registerMeal(mealDTO1);
                                boolean mealSuccess2 = mealService.registerMeal(mealDTO2);

                                // "선택안함"을 위한 MEAL 테이블에 INSERT 추가.
                                // 오름관2동이나 3동이 아닐 때만 추가
                                boolean mealSuccess3 = false;
                                if (!(parts.length == 5 && Integer.parseInt(parts[4]) == 0)) {
                                    MealDTO mealDTO3 = new MealDTO();
                                    mealDTO3.setDormitoryId(dormitoryId);
                                    mealDTO3.setName("선택안함");
                                    mealDTO3.setFee(0);
                                    mealSuccess3 = mealService.registerMeal(mealDTO3);
                                }
                                // MEAL 테이블 및 ROOM 테이블에 정상적으로 INSERT 및 UPDATE 되었을 경우 결과 메시지를 클라이언트에게 전송.
                                if (updateSuccess && mealSuccess1 && mealSuccess2) {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_FEE,
                                            Packet.SUCCESS,
                                            "생활관 사용료 및 급식비 등록이 완료되었습니다.");
                                    System.out.println("Fee registered successfully");
                                } else {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_FEE,
                                            Packet.FAIL,
                                            "생활관 사용료 및 급식비 등록에 실패했습니다.");
                                    System.out.println("Fee registration failed");
                                }
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            //관리자 기능 3번 (code 10) : 신청자 조회
                            case Packet.VIEW_APPLICANTS :
                                List<ApplicantInfoDTO> applicantList = applicantInfoDAO.getApplicantDormitoryInfo();
                                String applicantInfo = ApplicantViewService.ListToString(applicantList);

                                if (applicantList.isEmpty() || applicantInfo == null) {
                                    System.out.println("리스트 비어있음");
                                }

                                // 메시지로 작성 후 패킷화 해줌
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.VIEW_APPLICANTS,
                                        Packet.SUCCESS, applicantInfo);
                                packet = Packet.makePacket(txMsg);

                                // 스트림 통해서 보내줌
                                out.write(packet);
                                out.flush();
                                break;

                            //관리자 기능 4번 (code 11) : 입사자 선발 및 호실 배정
                            case Packet.SELECT_STUDENTS:
                                System.out.println("기숙사 배정 요청 처리 시작");

                                try {
                                    // 1. 신청자 정보 가져오기
                                    System.out.println("신청자 정보를 가져오는 중...");
                                    ApplicantService applicantService = new ApplicantService();
                                    List<ApplicantDTO> applicants = applicantService.getApplicantsWithScores();

                                    if (applicants == null || applicants.isEmpty()) {
                                        throw new IllegalStateException("신청자 정보가 없습니다.");
                                    }

                                    System.out.println("신청자 목록: " + applicants.toString());

                                    // 2. 기숙사 배정 수행
                                    System.out.println("기숙사 배정을 수행하는 중...");
                                    WinnerAssignmentService winnerAssignmentService = new WinnerAssignmentService();
                                    List<Map<String, List<ApplicantDTO>>> assignmentResult = winnerAssignmentService.assignApplicantsToDormitories(applicants);

                                    if (assignmentResult == null || assignmentResult.isEmpty()) {
                                        throw new IllegalStateException("배정 결과가 없습니다.");
                                    }

                                    System.out.println("배정 결과: " + assignmentResult.toString());

                                    // 3. 배정 결과를 DB에 저장
                                    System.out.println("배정 결과를 DB에 저장하는 중...");
                                    Map<String, List<ApplicantDTO>> maleDormitories = assignmentResult.get(0);
                                    Map<String, List<ApplicantDTO>> femaleDormitories = assignmentResult.get(1);

                                    winnerAssignmentService.saveDormitoryAssignments(maleDormitories);
                                    winnerAssignmentService.saveDormitoryAssignments(femaleDormitories);

                                    System.out.println("배정 결과 저장 완료!");

                                    // 4. 성공 응답 생성
                                    System.out.println("성공 응답을 생성하는 중...");
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.SELECT_STUDENTS, Packet.SUCCESS, "배정 완료");
                                    packet = Packet.makePacket(txMsg);
                                    out.write(packet);
                                    out.flush();
                                } catch (IllegalStateException e) {
                                    // 데이터 누락 등 논리적 문제 처리
                                    e.printStackTrace();
                                    System.err.println("논리적 오류 발생: " + e.getMessage());
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.SELECT_STUDENTS, Packet.FAIL, "배정 실패: " + e.getMessage());
                                    packet = Packet.makePacket(txMsg);
                                    try {
                                        out.write(packet);
                                        out.flush();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        System.err.println("실패 응답 전송 중 오류 발생: " + ex.getMessage());
                                    }
                                } catch (Exception e) {
                                    // 시스템 오류 처리
                                    e.printStackTrace();
                                    System.err.println("배정 중 시스템 오류 발생: " + e.getMessage());
                                    txMsg = Message.makeMessage(Packet.RESULT, Packet.SELECT_STUDENTS, Packet.FAIL, "배정 중 시스템 오류 발생");
                                    packet = Packet.makePacket(txMsg);
                                    try {
                                        out.write(packet);
                                        out.flush();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        System.err.println("실패 응답 전송 중 오류 발생: " + ex.getMessage());
                                    }
                                }
                                break;


                            //관리자 기능 5번 (code 12) : 생활관 비용 납부자 조회
                            case Packet.VIEW_PAID_STUDENTS :
                                // 요청 받음
                                List<StudentPaymentDTO> paidStudentList = studentPaymentDAO.getPaidStudentList();
                                String paidList = StudentPaymentCheckService.ListToString(paidStudentList);
                                System.out.println(paidList + "test");

                                if (paidStudentList.isEmpty())
                                    System.out.println("질의 똑바로 ㄴㄴ");

                                // 메시지로 작성 후 패킷화 해줌
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.VIEW_PAID_STUDENTS,
                                        Packet.SUCCESS, paidList);
                                packet = Packet.makePacket(txMsg);

                                // 스트림 통해서 보내줌
                                out.write(packet);
                                out.flush();
                                break;

                            //관리자 기능 6번 (code 13) : 생활관 비용 미납부자 조회
                            case Packet.VIEW_UNPAID_STUDENTS :
                                // 요청 받음
                                List<StudentPaymentDTO> unpaidStudentList = studentPaymentDAO.getUnpaidStudentList();
                                String unpaidList = StudentPaymentCheckService.ListToString(unpaidStudentList);

                                if (unpaidStudentList.isEmpty())
                                    System.out.println("질의 똑바로 ㄴㄴ");
                                System.out.println(unpaidList + "test");
                                // 메시지로 작성 후 패킷화 해줌
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.VIEW_UNPAID_STUDENTS,
                                        Packet.SUCCESS, unpaidList);
                                packet = Packet.makePacket(txMsg);

                                // 스트림 통해서 보내줌
                                out.write(packet);
                                out.flush();
                                break;

                            //2.7 결핵진단서 제출 확인 기능
                            case Packet.CHECK_CERTIFICATES:
                                // 데이터베이스에서 모든 결핵진단서 목록을 조회
                                List<TuberculosisDTO> certificates = tuberculosisService.getCertificates();

                                // 조회된 진단서 목록을 전송 가능한 문자열 형태로 포맷팅
                                // 형식: "학번,제출일,제출기한;" 형태의 문자열로 변환
                                String certListData = tuberculosisService.formatCertificateList(certificates);

                                // 응답 메시지 생성
                                // - Type: RESULT (처리 결과)
                                // - Code: CHECK_CERTIFICATES (결핵진단서 확인 기능)
                                // - Detail: SUCCESS (성공)
                                // - Data: 포맷팅된 진단서 목록 데이터
                                txMsg = Message.makeMessage(Packet.RESULT,
                                        Packet.CHECK_CERTIFICATES,
                                        Packet.SUCCESS,
                                        certListData);

                                // 생성된 메시지를 패킷으로 변환
                                packet = Packet.makePacket(txMsg);

                                // 클라이언트에 전송
                                out.write(packet);
                                out.flush();
                                break;

                            // 2.8 퇴사 신청자 조회 및 환불 처리 기능
                            case Packet.PROCESS_WITHDRAWAL:
                                // 클라이언트로부터 수신된 데이터 확인
                                if (rxMsg.getData().equals("approved_withdrawals")) {
                                    // 승인된 퇴사자 목록 조회
                                    WithdrawService withdrawService = new WithdrawService(); // WithdrawService 객체 생성
                                    String withdrawData = withdrawService.getApprovedWithdrawData(); // 승인된 퇴사 데이터 가져오기

                                    if (!withdrawData.isEmpty()) {
                                        // 승인된 퇴사자 데이터가 존재하면 성공 응답 메시지 생성
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.PROCESS_WITHDRAWAL, Packet.SUCCESS, withdrawData);
                                    } else {
                                        // 승인된 퇴사자 데이터가 없으면 실패 응답 메시지 생성
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.PROCESS_WITHDRAWAL, Packet.FAIL, "승인된 퇴사 신청자가 없습니다.");
                                    }
                                } else if (rxMsg.getData().equals("process_refunds")) {
                                    // 환불 처리 요청
                                    WithdrawService withdrawService = new WithdrawService(); // WithdrawService 객체 생성
                                    boolean success = withdrawService.processAllRefunds(); // 모든 환불 처리 수행

                                    if (success) {
                                        // 환불 처리 성공 시 성공 응답 메시지 생성
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.PROCESS_WITHDRAWAL, Packet.SUCCESS, "환불 처리 완료");
                                    } else {
                                        // 환불 처리 실패 시 실패 응답 메시지 생성
                                        txMsg = Message.makeMessage(Packet.RESULT, Packet.PROCESS_WITHDRAWAL, Packet.FAIL, "환불 처리 실패");
                                    }
                                }

                                // 생성된 메시지를 패킷으로 변환하여 클라이언트로 전송
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            case Packet.CHECK_DATE:
                                String feature;

                                if(rxMsg.getDetail() == Packet.APPLY_ADMISSION){
                                    feature = "생활관 입사 신청";
                                } else if (rxMsg.getDetail() == Packet.CHECK_ADMISSION) {
                                    feature = "생활관 배정 및 합격자 발표";
                                } else if (rxMsg.getDetail() == Packet.CHECK_PAY_DORMITORY) {
                                    feature = "생활관비 납부";
                                }else {
                                    feature = "결핵진단서 제출";
                                }


                                if (isSuccess(feature)) {
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESPONSE, Packet.CHECK_PAY_DORMITORY, Packet.SUCCESS, "")));
                                    out.flush();
                                } else {
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESPONSE, Packet.CHECK_PAY_DORMITORY, Packet.FAIL, "")));
                                    out.flush();
                                }

                                break;
                        }
                        break;

                    // TYPE = 0x02
                    case Packet.RESPONSE:
                        System.out.println("로그인 응답 정보 도착");
                        String data = rxMsg.getData();
                        if(data != null && !data.isEmpty()) {
                            String[] parts = data.split(",");
                            String id = parts[0];
                            String password = parts[1];
                            loggedInUserId = Integer.parseInt(id);
                            UserDTO user = userDAO.findUser(Integer.parseInt(id));
                            String gender = null;
                            boolean flag = false;

                            if (user.getRole().equals("학생"))
                            {
                                flag = true;
                                gender = studentDAO.getGender(Integer.parseInt(id));
                            }

                            boolean loginSuccess = (user != null) &&
                                    String.valueOf(user.getPassword()).equals(password);

                            if (loginSuccess && flag) {
                                String responseData = user.getId() + "," + user.getRole() + "," + gender;
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.LOGIN,
                                        Packet.SUCCESS, responseData);
                                studentID = user.getId();
                                System.out.println("User " + id + " logged in successfully");
                            }

                            else if (loginSuccess && !flag) {
                                gender = "123";
                                String responseData = user.getId() + "," + user.getRole() + "," + gender ;
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.LOGIN,
                                        Packet.SUCCESS, responseData);
                                studentID = user.getId();
                                System.out.println("User " + id + " logged in successfully");
                            }
                            else {
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.LOGIN,
                                        Packet.FAIL, "Login Failed");
                                System.out.println("Login failed for user " + id);
                            }
                            packet = Packet.makePacket(txMsg);
                            out.write(packet);
                            out.flush();
                        }
                        break;

                    case Packet.RESULT:
                        if(Packet.END_CONNECT == rxMsg.getDetail()){
                            return;
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    // 사용자가 입력한 생활관이름을 DB에 저장되어 있는 dormitory_id로 매핑하는 메소드.
    private int mapDormitoryToId(String dormitoryName) {
        switch (dormitoryName) {
            case "푸름관1동": return 1;
            case "푸름관2동": return 2;
            case "푸름관3동": return 3;
            case "푸름관4동": return 4;
            case "오름관1동": return 5;
            case "오름관2동": return 6;
            case "오름관3동": return 7;
            default: throw new IllegalArgumentException("Invalid dormitory name: " + dormitoryName);
        }
    }
    // 사용자가 입력한 급식비(5일식, 7일식, 선택안함)를 DB에 MEAL 테이블에서 meal_id로 매핑하는 과정.
    private int mapMealId(String dormitoryName, String mealType) {
        int dormitoryId = mapDormitoryToId(dormitoryName);
        if(mealType.equals("선택안함")){
            int mealId = mealService.getMealId(dormitoryId, "선택안함");
            return mealId;
        }else{
            int mealId = mealService.getMealId(dormitoryId, mealType);
            return mealId;
        }
    }
    // isWithinPeriod 메서드를 이용해 날짜에 따라 기능을 사용할 수 있는지 확인하는 메서드.
    public boolean isSuccess(String feature){
        ScheduleService scheduleService = new ScheduleService();
        List<ScheduleDTO> s = scheduleService.getSchedules();
        for(ScheduleDTO schedule : s){
            if(schedule.getPeriodName().equals(feature)){
                //String startDate, String startTime, String endDate, String endTime
                return isWithinPeriod(schedule.getStartDate(),schedule.getStartHour(),schedule.getEndDate(),schedule.getEndHour());
            }
        }
        return false;
    }
    // 선발일정에 대해 DB에 시작 날짜 및 종료날짜 사이에 현재 날짜가 포함되는지 확인하는 메서드.
    public static boolean isWithinPeriod(String startDate, String startTime, String endDate, String endTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 시작 시간 및 종료 시간
        LocalDateTime startDateTime = LocalDateTime.parse(startDate + " " + startTime, dateTimeFormatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate + " " + endTime, dateTimeFormatter);
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();
        System.out.println("현재 시간: " + now);
        System.out.println("시작 시간: " + startDateTime);
        System.out.println("종료 시간: " + endDateTime);
        // 현재 시간이 기간 내에 있는지 확인
        boolean result = (now.isEqual(startDateTime) || now.isAfter(startDateTime)) && (now.isEqual(endDateTime) || now.isBefore(endDateTime));
        System.out.println("결과: " + result);
        return result;
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