// ScheduleService.java
package Service;

import dao.ScheduleDAO;
import dto.ScheduleDTO;
import java.util.List;

public class ScheduleService {
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    // 선발 일정 조회
    public List<ScheduleDTO> getSchedules() {
        return scheduleDAO.getAllSchedules();
    }

    // 선발 일정 등록
    public boolean registerSchedule(ScheduleDTO schedule) {
        return scheduleDAO.registerSchedule(schedule);
    }

    // 일정 데이터 포맷팅
    public String formatScheduleData(List<ScheduleDTO> schedules) {
        if (schedules == null || schedules.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (ScheduleDTO schedule : schedules) {
            sb.append(schedule.getPeriodName()).append(",")
                    .append(schedule.getStartDate()).append(",")
                    .append(schedule.getEndDate()).append(";");
        }
        return sb.substring(0, sb.length() -1);
    }
}