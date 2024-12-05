// SelectionScheduleDAO.java
package dao;

import dto.SelectionScheduleDTO;
import javax.sql.DataSource;
import java.sql.*;

public class SelectionScheduleDAO { //DB 업데이트
    private final DataSource ds = PooledDataSource.getDataSource();

    // 선발 일정 등록
    public int registerSchedule(SelectionScheduleDTO schedule) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO selection_schedule (period_name, start_date, end_date) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);  // 생성된 키 반환 설정
            pstmt.setString(1, schedule.getPeriodName());
            pstmt.setTimestamp(2, schedule.getStartDate());
            pstmt.setTimestamp(3, schedule.getEndDate());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;  // 삽입 실패
            }

            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);  // 생성된 schedule_id 반환
            } else {
                return -1;  // ID 조회 실패
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            closeResources(conn, pstmt, rs);
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