package dao;

import dto.ScheduleDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 일정 관련 데이터베이스 접근을 담당하는 DAO 클래스
public class ScheduleDAO {
    // Connection Pool에서 데이터소스 가져오기
    private final DataSource ds = PooledDataSource.getDataSource();


    public List<ScheduleDTO> getAllSchedules() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ScheduleDTO> schedules = new ArrayList<>();

        try {
            conn = ds.getConnection();
            // 시작일 기준으로 정렬하여 모든 일정 조회
            String sql = "SELECT * FROM selection_schedule ORDER BY start_date";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // ResultSet의 각 행을 DTO 객체로 변환
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

    public boolean registerSchedule(ScheduleDTO schedule) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            // 일정 정보를 데이터베이스에 삽입
            String sql = "INSERT INTO selection_schedule (period_name, start_date, start_hour, end_date, end_hour) " +
                    "VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            // PreparedStatement에 파라미터 설정
            pstmt.setString(1, schedule.getPeriodName());
            pstmt.setString(2, schedule.getStartDate());
            pstmt.setString(3, schedule.getStartHour());
            pstmt.setString(4, schedule.getEndDate());
            pstmt.setString(5, schedule.getEndHour());

            // SQL 실행 및 결과 확인
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
            if (rs != null) rs.close();        // ResultSet 닫기
            if (pstmt != null) pstmt.close();  // PreparedStatement 닫기
            if (conn != null) conn.close();     // Connection 반환(커넥션 풀로)
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}