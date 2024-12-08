package network;

import Service.*;
import common.Packet;
import dto.*;
import dao.UserDAO;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Threads extends Thread {
    private Socket socket;
    private UserDTO userDTO;
    private UserDAO userDAO;

    private ScheduleService scheduleService;
    private RoomService roomService;
    private MealService mealService;
    private ApplicationService applicationService;
    private ApplicationPreferenceService applicationPreferenceService;
    private StudentService studentService;
    private AdmissionService admissionService;
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
        this.roomService = new RoomService();
        this.mealService = new MealService();
        this.applicationService = new ApplicationService();
        this.applicationPreferenceService = new ApplicationPreferenceService();
        this.studentService = new StudentService();
        this.admissionService = new AdmissionService();
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // 초기 로그인 요청 메시지 전송
            txMsg = Message.makeMessage(Packet.REQUEST, Packet.Login, Packet.NOT_USED, "Login Request");
            packet = Packet.makePacket(txMsg);
            out.write(packet);
            out.flush();

            while(true) {
                rxMsg = new Message();
                header = new byte[Packet.LEN_HEADER];
                in.read(header);
                Message.makeMessageHeader(rxMsg, header);
                body = new byte[rxMsg.getLength()];
                in.read(body);
                Message.makeMessageBody(rxMsg, body);
                Message.printMessage(rxMsg);
                byte type = rxMsg.getType();

                System.out.println("test" + type);

                switch(type) {
                    case Packet.REQUEST:
                        byte code = rxMsg.getCode();
                        System.out.println("코드 받음");

                        switch(code) {
                            case Packet.CHECK_SCHEDULE:
                                System.out.println("스케줄 조회 시작");
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
                                    // 데이터 형식 오류
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_SCHEDULE,
                                            Packet.FAIL,
                                            "입력 데이터 형식이 잘못되었습니다.");
                                    System.err.println("Data format error: " + e.getMessage());
                                } catch (Exception e) {
                                    // 기타 예외 처리
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_SCHEDULE,
                                            Packet.FAIL,
                                            "일정 등록 중 오류가 발생했습니다.");
                                    System.err.println("Unexpected error: " + e.getMessage());
                                }

                                // 결과 전송
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;
                            case Packet.REGISTER_FEE:
                                String feeData = rxMsg.getData();
                                // feeData 파싱 (형식: dormitoryName,dormitoryUsageFee,dormitoryMealFee)
                                String[] parts = feeData.split(",");
                                RoomDTO roomDTO = new RoomDTO();
                                MealDTO mealDTO = new MealDTO();
                                if(parts[0].equals("푸름관1동")){
                                    roomDTO.setDormitoryId(1);
                                    mealDTO.setDormitoryId(1);
                                }else if(parts[0].equals("푸름관2동")){
                                    roomDTO.setDormitoryId(2);
                                    mealDTO.setDormitoryId(2);
                                }else if(parts[0].equals("푸름관3동")){
                                    roomDTO.setDormitoryId(3);
                                    mealDTO.setDormitoryId(3);
                                }else if(parts[0].equals("푸름관4동")){
                                    roomDTO.setDormitoryId(4);
                                    mealDTO.setDormitoryId(4);
                                }else if(parts[0].equals("오름관1동")){
                                    roomDTO.setDormitoryId(5);
                                    mealDTO.setDormitoryId(5);
                                }else if(parts[0].equals("오름관2동")){
                                    roomDTO.setDormitoryId(6);
                                    mealDTO.setDormitoryId(6);
                                }else if(parts[0].equals("오름관3동")){
                                    roomDTO.setDormitoryId(7);
                                    mealDTO.setDormitoryId(7);
                                }

                                roomDTO.setFee(Integer.parseInt(parts[1]));
                                mealDTO.setFee(Integer.parseInt(parts[2]));
                                boolean roomSuccess = roomService.registerRoom(roomDTO);
                                boolean mealSuccess = mealService.registerMeal(mealDTO);
                                if (roomSuccess & mealSuccess) {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_FEE,
                                            Packet.SUCCESS,
                                            "생활관 사용료 및 급식비 등록이 완료되었습니다.");
                                    System.out.println("Schedule registered successfully: ");
                                } else {
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_FEE,
                                            Packet.FAIL,
                                            "생활관 사용료 및 급식비 등록에 실패했습니다.");
                                    System.out.println("Schedule registration failed");
                                }
                                // 결과 전송
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;
                            case Packet.APPLY_ADMISSION:
                                String admissionData = rxMsg.getData();
                                String[] admissionParts = admissionData.split(",");
                                //firstDormitory + "," + firstDormitoryMeal + "," + secondDormitory + "," + secondDormitoryMeal;
                                ApplicationDTO applicationDTO = new ApplicationDTO();
                                ApplicationPreferenceDTO applicationPreferenceDTO = new ApplicationPreferenceDTO();

                                applicationDTO.setStudent_id(loggedInUserId);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                applicationDTO.setApplication_date(LocalDate.now().format(formatter));
                                boolean applicationSuccess = applicationService.applyAdmission(applicationDTO);

                                //applicationPreference
                                // application.getApplicationId();
                                // 2번 연속 등록 시도 해야함. getApplication_id를 잘 가져오는지.
                                // !! setApplication_id의 값은 방금 정의한 application_id의 값을 대입해야함.
                                // 현재 안됨. 임시로 1로 해둠.

                                int applicationId = applicationService.findApplicationId(loggedInUserId);

                                applicationPreferenceDTO.setApplication_id(applicationId);
                                if(admissionParts[0].equals("푸름관1동")){
                                    applicationPreferenceDTO.setPreference_first(1);
                                }else if(admissionParts[0].equals("푸름관2동")){
                                    applicationPreferenceDTO.setPreference_first(2);
                                }else if(admissionParts[0].equals("푸름관3동")){
                                    applicationPreferenceDTO.setPreference_first(3);
                                }else if(admissionParts[0].equals("푸름관4동")){
                                    applicationPreferenceDTO.setPreference_first(4);
                                }else if(admissionParts[0].equals("오름관1동")){
                                    applicationPreferenceDTO.setPreference_first(5);
                                }else if(admissionParts[0].equals("오름관2동")){
                                    applicationPreferenceDTO.setPreference_first(6);
                                }else if(admissionParts[0].equals("오름관3동")){
                                    applicationPreferenceDTO.setPreference_first(7);
                                }

                                applicationPreferenceDTO.setMeal_first(admissionParts[1]);
                                if(admissionParts[2].equals("푸름관1동")){
                                    applicationPreferenceDTO.setPreference_second(1);
                                }else if(admissionParts[2].equals("푸름관2동")){
                                    applicationPreferenceDTO.setPreference_second(2);
                                }else if(admissionParts[2].equals("푸름관3동")){
                                    applicationPreferenceDTO.setPreference_second(3);
                                }else if(admissionParts[2].equals("푸름관4동")){
                                    applicationPreferenceDTO.setPreference_second(4);
                                }else if(admissionParts[2].equals("오름관1동")){
                                    applicationPreferenceDTO.setPreference_second(5);
                                }else if(admissionParts[2].equals("오름관2동")){
                                    applicationPreferenceDTO.setPreference_second(6);
                                }else if(admissionParts[2].equals("오름관3동")){
                                    applicationPreferenceDTO.setPreference_second(7);
                                }
                                applicationPreferenceDTO.setMeal_second(admissionParts[3]);
                                boolean applicationPreferenceSuccess = applicationPreferenceService.applyPreference(applicationPreferenceDTO);
                                if (applicationSuccess & applicationPreferenceSuccess) {
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
                            case Packet.CHECK_ADMISSION:
                                System.out.println("합격 여부 및 호실 확인 요청 받음");
                                AdmissionDTO admissionDTO = new AdmissionDTO();

                                boolean admissionSuccess = admissionService.findCheckAdmission();
                                String newData = admissionDTO.getRoom_id() + "," + admissionDTO.getAdmission_status();
                                if(admissionSuccess){
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.CHECK_ADMISSION,
                                            Packet.SUCCESS,
                                            newData);
                                    System.out.println("check admission successfully: ");
                                }else{
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.CHECK_ADMISSION,
                                            Packet.FAIL,
                                            "합격 여부 및 호실 확인 실패");
                                    System.out.println("check admission failed");
                                }
                                packet = Packet.makePacket(txMsg);
                                out.write(packet);
                                out.flush();
                                break;

                        }
                        break;


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
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.Login,
                                        Packet.SUCCESS, responseData);
                                System.out.println("User " + id + " logged in successfully");
                            } else {
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.Login,
                                        Packet.FAIL, "Login Failed");
                                System.out.println("Login failed for user " + id);
                            }
                            packet = Packet.makePacket(txMsg);
                            out.write(packet);
                            out.flush();
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