package network;

import dto.*;
import service.*;
import common.Packet;
import dao.UserDAO;
import dao.RoomDAO;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;

public class Threads extends Thread {
    private Socket socket;
    private UserDTO userDTO;
    private UserDAO userDAO;
    private ScheduleService scheduleService;
    private TuberculosisService tuberculosisService;
    private RoomService roomService;
    private MealService mealService;
    private DataInputStream in;
    private DataOutputStream out;
    byte[] header = null;
    byte[] body = null;
    byte[] packet = null;
    Message txMsg = null;
    Message rxMsg = null;

    public Threads(Socket socket) {
        this.socket = socket;
        this.userDAO = new UserDAO();
        this.scheduleService = new ScheduleService();
        this.tuberculosisService = new TuberculosisService();
        this.roomService = new RoomService();
        this.mealService = new MealService();
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

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
                                String scheduleData = rxMsg.getData();
                                try {
                                    String[] parts = scheduleData.split(",");
                                    if (parts.length != 3) {
                                        throw new IllegalArgumentException("잘못된 데이터 형식");
                                    }

                                    ScheduleDTO newSchedule = new ScheduleDTO();
                                    newSchedule.setPeriodName(parts[0]);

                                    String[] startDateTime = parts[1].split(" ");
                                    String[] endDateTime = parts[2].split(" ");

                                    newSchedule.setStartDate(startDateTime[0]);
                                    newSchedule.setStartHour(startDateTime[1]);
                                    newSchedule.setEndDate(endDateTime[0]);
                                    newSchedule.setEndHour(endDateTime[1]);

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
                        }
                        break;

                    case Packet.RESPONSE:
                        System.out.println("로그인 응답 정보 도착");
                        String data = rxMsg.getData();
                        if(data != null && !data.isEmpty()) {
                            String[] parts = data.split(",");
                            String id = parts[0];
                            String password = parts[1];

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