package network;

import common.Packet;
import Service.ScheduleService;
import dto.ScheduleDTO;
import dto.UserDTO;
import dao.UserDAO;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class Threads extends Thread {
    private Socket socket;
    private UserDTO userDTO;
    private UserDAO userDAO;
    private ScheduleService scheduleService;
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

                                    // DTO 생성 및 설정
                                    ScheduleDTO newSchedule = new ScheduleDTO();
                                    newSchedule.setPeriodName(parts[0]);

                                    // 문자열을 Timestamp로 변환
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                    dateFormat.parse(parts[1]);
                                    dateFormat.parse(parts[2]);
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

                                } catch (ParseException e) {
                                    // 날짜 형식 파싱 실패
                                    txMsg = Message.makeMessage(Packet.RESULT,
                                            Packet.REGISTER_SCHEDULE,
                                            Packet.FAIL,
                                            "날짜 형식이 잘못되었습니다. (yyyy-MM-dd HH:mm:ss)");
                                    System.err.println("Date parsing error: " + e.getMessage());
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
                                txMsg = Message.makeMessage(Packet.RESULT, Packet.Login,
                                        Packet.SUCCESS, user.getRole());
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