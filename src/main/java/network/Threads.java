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


    private RoomService roomService;
    private MealService mealService;
    private ApplicationService applicationService;
    private ApplicationPreferenceService applicationPreferenceService;
    private StudentService studentService;
    private AdmissionService admissionService;
    private TuberculosisService tuberculosisService;
    private ScheduleService scheduleService;
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

                            case Packet.CHECK_PAY_DORMITORY:
                                ApplicationDTO applicationDTO = applicationDAO.getApplicationInfo(studentID);
                                AdmissionDTO admissionDTO = admissionDAO.findAdmission(studentID);
                                //ApplicationPreferenceDTO applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId());

                                ApplicationPreferenceDTO applicationPreferenceDTO;
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

                            case Packet.APPLY_ADMISSION:
                                String admissionData = rxMsg.getData();
                                String[] admissionParts = admissionData.split(",");
                                //firstDormitory + "," + firstDormitoryMeal + "," + secondDormitory + "," + secondDormitoryMeal;
                                applicationDTO = new ApplicationDTO();
                                applicationPreferenceDTO = new ApplicationPreferenceDTO();

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

                            case Packet.PROCESS_WITHDRAWAL:
                                System.out.println("퇴사 신청자 조회 시작");
                                WithdrawService withdrawService = new WithdrawService();
                                String withdrawData = withdrawService.getWithdrawAndRefundData();

                                if (!withdrawData.isEmpty()) {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.PROCESS_WITHDRAWAL,
                                            Packet.SUCCESS,
                                            withdrawData);
                                } else {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.PROCESS_WITHDRAWAL,
                                            Packet.FAIL,
                                            "퇴사 신청자가 없습니다.");
                                }
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;


                            case Packet.REQUEST_WITHDRAWAL:
                                LocalDate now = LocalDate.now();
                                applicationDTO = applicationDAO.getApplicationInfo(studentID);
                                admissionDTO = admissionDAO.findAdmission(studentID);
                                //ApplicationPreferenceDTO applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId());

                                if (admissionDTO == null) {
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.REQUEST_WITHDRAWAL, Packet.FAIL, "퇴사 신청 대상자가 아닙니다.")));
                                    out.flush();
                                } else {
                                    applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId(),admissionDTO.getDormitoryId());

                                    WithdrawDTO withdraw = new WithdrawDTO();
                                    String data = rxMsg.getData();

                                    String[] parts = data.split(",");
                                    String bankName = parts[0];
                                    String accountNumber = parts[1];
                                    String reason = parts[2];

                                    withdraw.setStudentId(studentID);
                                    withdraw.setApplicationDate(now.toString());

                                    //퇴사 상태
                                    RoomDTO roomDTO = roomDAO.getRoomInfo(admissionDTO.getRoomId());
                                    MealDTO mealDTO = mealDAO.getMealInfo(applicationPreferenceDTO.getMeal_id());
                                    int totalFee = roomDTO.getFee() + mealDTO.getFee();

                                    if (now.isBefore(admissionDTO.getResidenceStartDate())) {
                                        withdraw.setWithdrawalType("입사 전");
                                        //환불 금액
                                        withdraw.setRefundAmount(totalFee);
                                    } else {
                                        Period remainPeriod = Period.between(now, admissionDTO.getResidenceEndDate());
                                        Period totalPeriod = Period.between(admissionDTO.getResidenceStartDate(), admissionDTO.getResidenceEndDate());
                                        withdraw.setWithdrawalType("입사 후");

                                        //환불 금액
                                        int date = remainPeriod.getYears() * 365 + remainPeriod.getMonths() * 30 + remainPeriod.getDays();
                                        int totalDate = totalPeriod.getYears() * 365 + totalPeriod.getMonths() * 30 + totalPeriod.getDays();
                                        int fee = (totalFee / totalDate) * date;
                                        withdraw.setRefundAmount(fee);
                                    }


                                    withdraw.setBankName(bankName);
                                    withdraw.setAccountNumber(parseInt(accountNumber));

                                    withdraw.setWithdrawalStatus("취소");
                                    withdraw.setReason(reason);
                                    //생활관 아이디
                                    withdraw.setDormitoryId(applicationPreferenceDTO.getDormitory_id());

                                    withdrawDAO.setWithdrawInfo(withdraw);

                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.REQUEST_WITHDRAWAL, Packet.SUCCESS, "")));
                                    out.flush();
                                }
                                break;
                            case Packet.CHECK_REFUND:
                                WithdrawDTO withdraw;
                                withdraw = withdrawDAO.getWithdrawInfo(studentID);

                                if (withdraw == null) {
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.CHECK_REFUND, Packet.FAIL, "환불 신청을 하지 않았거나 대상자가 아닙니다.")));
                                    out.flush();
                                } else {
                                    String newData = withdraw.getWithdrawalStatus();
                                    out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.CHECK_REFUND, Packet.SUCCESS, newData)));
                                    out.flush();
                                }
                                break;


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
                                    int studentId = parseInt(certParts[0]);
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
                                                "제출 성공");
                                    } else {
                                        txMsg = Message.makeMessage(Packet.RESULT,
                                                Packet.SUBMIT_CERTIFICATE,
                                                Packet.FAIL,
                                                submitResult);
                                    }
                                }
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

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

                            case Packet.REGISTER_FEE:
                                String feeData = rxMsg.getData();
                                String[] parts = feeData.split(",");
                                RoomDTO roomDTO = new RoomDTO();

                                if (parts[0].equals("푸름관1동")) {
                                    roomDTO.setDormitoryId(1);
                                } else if (parts[0].equals("푸름관2동")) {
                                    roomDTO.setDormitoryId(2);
                                } else if (parts[0].equals("푸름관3동")) {
                                    roomDTO.setDormitoryId(3);
                                } else if (parts[0].equals("푸름관4동")) {
                                    roomDTO.setDormitoryId(4);
                                } else if (parts[0].equals("오름관1동")) {
                                    roomDTO.setDormitoryId(5);
                                } else if (parts[0].equals("오름관2동")) {
                                    roomDTO.setDormitoryId(6);
                                } else if (parts[0].equals("오름관3동")) {
                                    roomDTO.setDormitoryId(7);
                                }
                                roomDTO.setFee(parseInt(parts[1]));
                                //Room 등록
                                boolean roomSuccess = roomService.registerRoom(roomDTO);

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

                                if (roomSuccess && mealSuccess1 && mealSuccess2) {
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

                            // 입사 신청자 조회
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



                                // 2.5 생활관 비용 납부자 조회 기능
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

                            // 2.6 생활관 비용 미납부자 조회 기능
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

                            default:
                                throw new IllegalStateException("Unexpected value: " + code);
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
                            loggedInUserId = parseInt(id);
                            UserDTO user = userDAO.findUser(parseInt(id));
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