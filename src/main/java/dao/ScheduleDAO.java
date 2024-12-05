// SelectionScheduleDAO.java
package dao;

import dto.ScheduleDTO;
import javax.sql.DataSource;
import java.sql.*;

public class ScheduleDAO { //DB 업데이트
    private final DataSource ds = PooledDataSource.getDataSource();

    // 스케쥴 가져오기
    public ScheduleDTO getSchedule() {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ScheduleDTO scheduleDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM selection_schedule WHERE period_name = ? AND start_date = ? AND end_date = ?";
            // String sql = "SELECT * FROM selection_schedule selection_schedule (period_name, start_date, end_date) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);  // 생성된 키 반환 설정
            pstmt.setString(1, scheduleDTO.getPeriodName());
            pstmt.setString(2, scheduleDTO.getStartDate());
            pstmt.setString(3, scheduleDTO.getEndDate());

            rs = pstmt.executeQuery();

            if (rs.next()) {
                scheduleDTO.setPeriodName(rs.getString("period_name"));
                scheduleDTO.setStartDate(rs.getString("start_date"));
                scheduleDTO.setEndDate(rs.getString("end_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return scheduleDTO;
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