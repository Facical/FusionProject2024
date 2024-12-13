package service;

import dao.ScheduleDAO;
import dto.ScheduleDTO;
import java.util.List;

public class ScheduleService {
    private final ScheduleDAO scheduleDAO = new ScheduleDAO(); // 데이터 접근 객체 생성 및 초기화

    // 선발 일정 조회
    // 데이터베이스에서 모든 선발 일정을 가져옵니다.
    public List<ScheduleDTO> getSchedules() {
        return scheduleDAO.getAllSchedules();
    }

    // 선발 일정 등록
    // 주어진 ScheduleDTO 객체를 데이터베이스에 등록합니다.
    // 성공 시 true, 실패 시 false를 반환합니다.
    public boolean registerSchedule(ScheduleDTO schedule) {
        return scheduleDAO.registerSchedule(schedule);
    }

    // 일정 데이터 포맷팅
    // ScheduleDTO 리스트를 특정 문자열 형식으로 변환합니다.
    // 반환 형식: "기간명,시작일,시작시간,종료일,종료시간;" 형태의 데이터
    public String formatScheduleData(List<ScheduleDTO> schedules) {
        if (schedules == null || schedules.isEmpty()) return ""; // 일정이 없으면 빈 문자열 반환

        StringBuilder sb = new StringBuilder(); // 데이터 포맷팅을 위한 StringBuilder 사용
        for (ScheduleDTO schedule : schedules) {
            // 각 ScheduleDTO 데이터를 ','로 구분하여 문자열로 변환
            sb.append(schedule.getPeriodName()).append(",")
                    .append(schedule.getStartDate()).append(",")
                    .append(schedule.getStartHour()).append(",")
                    .append(schedule.getEndDate()).append(",")
                    .append(schedule.getEndHour()).append(";");
        }
        // 마지막 ';' 제거 후 반환
        return sb.substring(0, sb.length() - 1);
    }
}