package dao;

import dto.ApplicationDTO;
import javax.sql.DataSource;
import java.sql.*;

public class ApplicationDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public ApplicationDTO getApplicationInfo(int studentID){
        /*
        학번을 받아 해당 학번의 학생의 신청 정보를 가져오는 함수
         */
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ApplicationDTO applicationDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM application WHERE student_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                applicationDTO = new ApplicationDTO();
                applicationDTO.setApplicationId(rs.getInt("application_id"));
                applicationDTO.setStudentId(rs.getInt("student_id"));
                applicationDTO.setApplicationDate(rs.getString("application_date"));
                applicationDTO.setScheduleId(rs.getInt("schedule_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return applicationDTO;

    }

    public void setApplicationInfo(ApplicationDTO applicationDTO){
        Connection conn = null;
        PreparedStatement pstmt = null;
        int updateResult;

        /*
        application_id 를 지정해서 넣는 것이 맞는 건가?
         */

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO application (student_id, application_date, schedule_id) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationDTO.getStudentId());
            pstmt.setString(2, applicationDTO.getApplicationDate());
            pstmt.setInt(3, applicationDTO.getScheduleId());
            updateResult = pstmt.executeUpdate();

            if (updateResult > 0) {
                System.out.println("Update successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt);
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

    private void closeResources(Connection conn, PreparedStatement pstmt) {
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
