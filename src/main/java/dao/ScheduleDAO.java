// ScheduleDAO.java
package dao;

import dto.ScheduleDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    private final DataSource ds = PooledDataSource.getDataSource();
    private ScheduleDTO scheduleDTO;// = null;

    // 모든 선발 일정 조회
    public List<ScheduleDTO> getAllSchedules() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ScheduleDTO> schedules = new ArrayList<>();

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM selection_schedule ORDER BY start_date";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ScheduleDTO schedule = new ScheduleDTO();
                schedule.setScheduleId(rs.getInt("schedule_id"));
                schedule.setPeriodName(rs.getString("period_name"));
                schedule.setStartDate(rs.getString("start_date"));
                schedule.setStartHour(rs.getString("start_hour"));
                schedule.setEndDate(rs.getString("end_date"));
                schedule.setEndHour(rs.getString("end_hour"));
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return schedules;
    }

    // 선발 일정 등록
    public boolean registerSchedule(ScheduleDTO schedule) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO selection_schedule (period_name, start_date, start_hour, end_date, end_hour) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getPeriodName());
            pstmt.setString(2, schedule.getStartDate());
            pstmt.setString(3, schedule.getStartHour());
            pstmt.setString(4, schedule.getEndDate());
            pstmt.setString(5, schedule.getEndHour());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}