// Threads.java
package network;

import common.Packet;
import dao.*;
import dto.*;


import java.io.*;
import java.net.Socket;
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
        this.scheduleDAO = new ScheduleDAO();
        this.withdrawDAO = new WithdrawDAO();
        this.admissionDAO = new AdmissionDAO();
        this.applicationPreferenceDAO = new ApplicationPreferenceDAO();
        this.applicationDAO = new ApplicationDAO();
        this.roomDAO = new RoomDAO();
        this.mealDAO = new MealDAO();
    }

    public void run() {
        try {
            int studentID = 0;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while(true) {

                Message rxMsg = Message.readMessage(in);
                Message.printMessage(rxMsg);

                byte type = rxMsg.getType();
                byte code = rxMsg.getCode();

                if(type == Packet.REQUEST){
                    if(code == Packet.Login){
                        System.out.println("로그인 응답 정보 도착");
                        String data = rxMsg.getData();
                        if(data != null && !data.isEmpty()) {
                            String[] parts = data.split(",");
                            String id = parts[0];
                            String password = parts[1];

                            UserDTO user = userDAO.findUser(parseInt(id));
                            boolean loginSuccess = (user != null) &&
                                    String.valueOf(user.getPassword()).equals(password);


                            if (loginSuccess) {
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.Login,
                                        Packet.SUCCESS, user.getRole());

                                System.out.println("User " + id + " logged in successfully");
                                studentID = parseInt(id);

                            } else {
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.Login,
                                        Packet.FAIL, "Login Failed");
                                System.out.println("Login failed for user " + id);
                            }
                            packet = Packet.makePacket(txMsg);
                            out.write(packet);
                            out.flush();
                        }
                    }
                    else if (code == Packet.CHECK_SCHEDULE) {
                        System.out.println("스케쥴 조회전");

                        ScheduleDTO schedule;

                        schedule = scheduleDAO.getSchedule();

                        String newData = schedule.getPeriodName() + "," + schedule.getStartDate() +"," + schedule.getEndDate();
                        System.out.println(newData);

                        txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.SUCCESS, newData);
                        packet = Packet.makePacket(txMsg);
                        out.write(packet);
                        out.flush();
                    }
                    // 생활관 비용 확인 및 납부
                    else if (code == Packet.CHECK_PAY_DORMITORY) {
                        ApplicationDTO applicationDTO = applicationDAO.getApplicationInfo(studentID);
                        AdmissionDTO admissionDTO = admissionDAO.findAdmission(studentID);
                        ApplicationPreferenceDTO applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId());

                        RoomDTO roomDTO = roomDAO.getRoomInfo(admissionDTO.getRoomId());
                        MealDTO mealDTO = mealDAO.getMealInfo(applicationPreferenceDTO.getMeal_id());
                        int totalFee = roomDTO.getFee() + mealDTO.getFee();

                        String data = roomDTO.getFee() + "," + mealDTO.getFee() + "," + totalFee + "," + admissionDTO.getPaymentStatus();

                        out.write(Packet.makePacket(Message.makeMessage(Packet.RESPONSE, Packet.CHECK_PAY_DORMITORY, Packet.SUCCESS, data)));
                        out.flush();

                        rxMsg = Message.readMessage(in);
                        Message.printMessage(rxMsg);


                        if(rxMsg.getDetail() == Packet.SUCCESS){
                            admissionDTO.setPaymentStatus("납부 완료");
                            admissionDAO.UpdatePaymentStatus(admissionDTO);
                        }


                        admissionDTO = admissionDAO.findAdmission(studentID);
                        if(admissionDTO.getPaymentStatus().equals("납부 완료")){
                            txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.SUCCESS, "");
                        } else if (admissionDTO.getPaymentStatus().equals("미납")) {
                            txMsg = Message.makeMessage(Packet.RESULT, Packet.CHECK_SCHEDULE, Packet.FAIL, "");
                        }

                        packet = Packet.makePacket(txMsg);
                        out.write(packet);
                        out.flush();
                    }
                    // 환불 신청
                    else if (code == Packet.REQUEST_WITHDRAWAL) {
                        LocalDate now = LocalDate.now();
                        ApplicationDTO applicationDTO = applicationDAO.getApplicationInfo(studentID);
                        AdmissionDTO admissionDTO = admissionDAO.findAdmission(studentID);
                        ApplicationPreferenceDTO applicationPreferenceDTO = applicationPreferenceDAO.getApplicationPreference(applicationDTO.getApplicationId());
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

                        if(now.isBefore(admissionDTO.getResidenceStartDate())){
                            withdraw.setWithdrawalType("입사 전");
                            //환불 금액
                            withdraw.setRefundAmount(totalFee);
                        }
                        else{
                            Period period = Period.between(now, admissionDTO.getResidenceEndDate());
                            Period totalPeriod = Period.between(admissionDTO.getResidenceStartDate(), admissionDTO.getResidenceEndDate());
                            withdraw.setWithdrawalType("입사 후");

                            //환불 금액
                            int date = period.getYears() * 365 + period.getMonths() * 30 + period.getDays();
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
                    // 환불 처리 확인
                    else if (code == Packet.CHECK_REFUND) {
                        WithdrawDTO withdraw;
                        withdraw = withdrawDAO.getWithdrawInfo(studentID);
                        String newData = withdraw.getWithdrawalStatus();
                        out.write(Packet.makePacket(Message.makeMessage(Packet.RESULT, Packet.CHECK_REFUND, Packet.NOT_USED, newData)));
                        out.flush();
                    }
                }
                else if(type == Packet.RESPONSE){

                }
                else if(type == Packet.RESULT){
                    if(Packet.END_CONNECT == rxMsg.getDetail()){
                        //socket.close();
                        return;
                    }
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