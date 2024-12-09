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
    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;
    Message txMsg = null;
    Message rxMsg = null;
    private static int loggedInUserId = -1;
    public Threads(Socket socket) {
        this.socket = socket;
        this.userDAO = new UserDAO();
        this.scheduleService = new ScheduleService();
        this.tuberculosisService = new TuberculosisService();
        this.roomService = new RoomService();
        this.mealService = new MealService();
        this.scheduleDAO = new ScheduleDAO();
        this.withdrawDAO = new WithdrawDAO();
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
            int studentID = 0;
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
                            //학생 기능 1번 (code 1) : 선발 일정 및 비용 확인
                            case Packet.CHECK_SCHEDULE:
                                List<ScheduleDTO> schedules = scheduleService.getSchedules();

                                if (!schedules.isEmpty()) {
                                    String scheduleData = scheduleService.formatScheduleData(schedules);
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.CHECK_SCHEDULE,
                                            Packet.SUCCESS,
                                            scheduleData);
                                } else {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.CHECK_SCHEDULE,
                                            Packet.FAIL,
                                            "일정이 없습니다.");
                                }
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

                            //학생 기능 5번 (code 5) : 결핵진단서 제출
                            case Packet.SUBMIT_CERTIFICATE:
                                String[] certParts = rxMsg.getData().split(",");

                                // 다운로드 요청 처리
                                if (certParts[0].equals("DOWNLOAD")) {
                                    String savePath = certParts[1];
                                    List<TuberculosisDTO> allCertificates = tuberculosisService.getCertificates();
                                    boolean downloadSuccess = true;
                                    String errorMessage = "";

                                    for (TuberculosisDTO certDTO : allCertificates) {
                                        try {
                                            if (certDTO.getImageData() != null) {
                                                String fileName = certDTO.getStudentId() + "_" + certDTO.getFileName();
                                                String filePath = savePath + File.separator + fileName;
                                                Files.write(Paths.get(filePath), certDTO.getImageData());
                                            }
                                        } catch (IOException e) {
                                            downloadSuccess = false;
                                            errorMessage = e.getMessage();
                                            break;
                                        }
                                    }

                                    if (downloadSuccess) {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.SUBMIT_CERTIFICATE,
                                                Packet.SUCCESS,
                                                "모든 진단서 다운로드 완료");
                                    } else {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.SUBMIT_CERTIFICATE,
                                                Packet.FAIL,
                                                "다운로드 실패: " + errorMessage);
                                    }
                                }
                                // 제출 처리
                                else {
                                    int studentId = Integer.parseInt(certParts[0]);
                                    byte[] fileData = Base64.getDecoder().decode(certParts[1]);
                                    String fileName = certParts[2];
                                    String fileType = certParts[3];

                                    TuberculosisDTO cert = new TuberculosisDTO();
                                    cert.setStudentId(studentId);
                                    cert.setImageData(fileData);
                                    cert.setFileName(fileName);
                                    cert.setFileType(fileType);

                                    String submitResult = tuberculosisService.submitCertificate(cert);

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

                            //관리자 기능 1번 (code 8) : 선발 일정 등록
                            case Packet.REGISTER_SCHEDULE:
                                // 관리자의 일정 등록 처리
                                String scheduleData = rxMsg.getData();
                                try {
                                    // scheduleData 파싱 (형식: periodName,startDate,endDate)
                                    String[] parts = scheduleData.split(",");
                                    if (parts.length != 3) {
                                        throw new IllegalArgumentException("잘못된 데이터 형식");
                                    }

                                    ScheduleDTO newSchedule = new ScheduleDTO();
                                    newSchedule.setPeriodName(parts[0]);

// 날짜와 시간 분리
                                    String[] startDateTime = parts[1].split(" ");
                                    String[] endDateTime = parts[2].split(" ");

                                    newSchedule.setStartDate(startDateTime[0]);
                                    newSchedule.setStartHour(startDateTime[1]);
                                    newSchedule.setEndDate(endDateTime[0]);
                                    newSchedule.setEndHour(endDateTime[1]);
                                    // 서비스를 통해 일정 등록
                                    boolean success = scheduleService.registerSchedule(newSchedule);

                                    if (success) {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.REGISTER_SCHEDULE,
                                                Packet.SUCCESS,
                                                "일정 등록이 완료되었습니다.");
                                        System.out.println("Schedule registered successfully: " + newSchedule.getPeriodName());
                                    } else {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.REGISTER_SCHEDULE,
                                                Packet.FAIL,
                                                "일정 등록에 실패했습니다.");
                                        System.out.println("Schedule registration failed");
                                    }

                                } catch (IllegalArgumentException e) {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_SCHEDULE,
                                            Packet.FAIL,
                                            "입력 데이터 형식이 잘못되었습니다.");
                                    System.err.println("Data format error: " + e.getMessage());
                                } catch (Exception e) {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_SCHEDULE,
                                            Packet.FAIL,
                                            "일정 등록 중 오류가 발생했습니다.");
                                    System.err.println("Unexpected error: " + e.getMessage());
                                }

                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            //관리자 기능 2번 (code 9) : 생활관 사용료 및 급식비 등록
                            case Packet.REGISTER_FEE:
                                String feeData = rxMsg.getData();
                                String[] parts = feeData.split(",");
                                RoomDTO roomDTO = new RoomDTO();
                                int dormitory_id = -1;
                                int fee = Integer.parseInt(parts[1]);
                                boolean updateSuccess = false;
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

                                //Room 등록
//                                boolean roomSuccess = roomService.registerRoom(roomDTO);

                                int dormitoryId = roomDTO.getDormitoryId();

                                MealDTO mealDTO1 = new MealDTO(); // 7일식
                                mealDTO1.setDormitoryId(dormitoryId);
                                mealDTO1.setName("7일식");
                                mealDTO1.setFee(Integer.parseInt(parts[3])); // sevenMealFee

                                MealDTO mealDTO2 = new MealDTO(); // 5일식
                                mealDTO2.setDormitoryId(dormitoryId);
                                mealDTO2.setName("5일식");
                                mealDTO2.setFee(Integer.parseInt(parts[2])); // fiveMealFee
                                // Meal 등록
                                boolean mealSuccess1 = mealService.registerMeal(mealDTO1);
                                boolean mealSuccess2 = mealService.registerMeal(mealDTO2);

                                // 선택안함 급식비 등록 (오름관2동이나 3동이 아닐 때만 추가)
                                boolean mealSuccess3 = false;
                                if (!(parts.length == 5 && Integer.parseInt(parts[4]) == 0)) {
                                    MealDTO mealDTO3 = new MealDTO();
                                    mealDTO3.setDormitoryId(dormitoryId);
                                    mealDTO3.setName("선택안함");
                                    mealDTO3.setFee(0); // 급식비 0원

                                    mealSuccess3 = mealService.registerMeal(mealDTO3);
                                }

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

                            //관리자 기능 7번 (code 14) : 결핵진단서 제출 확인
                            case Packet.CHECK_CERTIFICATES:
                                List<TuberculosisDTO> certificates = tuberculosisService.getCertificates();
                                String certListData = tuberculosisService.formatCertificateList(certificates);

                                txMsg = Message.makeMessage(Packet.RESULT,
                                        Packet.CHECK_CERTIFICATES,
                                        Packet.SUCCESS,
                                        certListData);
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                            //관리자 기능 8번 (code 15) : 퇴사 신청자 조회 및 환불
                            case Packet.PROCESS_WITHDRAWAL:
                                if (rxMsg.getData().equals("approved_withdrawals")) {
                                    WithdrawService withdrawService = new WithdrawService();
                                    String withdrawData = withdrawService.getApprovedWithdrawData();

                                    if (!withdrawData.isEmpty()) {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.PROCESS_WITHDRAWAL,
                                                Packet.SUCCESS,
                                                withdrawData);
                                    } else {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.PROCESS_WITHDRAWAL,
                                                Packet.FAIL,
                                                "승인된 퇴사 신청자가 없습니다.");
                                    }
                                } else if (rxMsg.getData().equals("process_refunds")) {
                                    WithdrawService withdrawService = new WithdrawService();
                                    boolean success = withdrawService.processAllRefunds();

                                    if (success) {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.PROCESS_WITHDRAWAL,
                                                Packet.SUCCESS,
                                                "환불 처리 완료");
                                    } else {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.PROCESS_WITHDRAWAL,
                                                Packet.FAIL,
                                                "환불 처리 실패");
                                    }
                                }
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
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
                            boolean loginSuccess = (user != null) &&
                                    String.valueOf(user.getPassword()).equals(password);

                            if(loginSuccess) {
                                String responseData = user.getId() + "," + user.getRole();
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.LOGIN,
                                        Packet.SUCCESS, responseData);
                                studentID = user.getId();
                                System.out.println("User " + id + " logged in successfully");
                            } else {
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