package dao;

import dto.*;

import dto.ApplicationDTO;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

public class ApplicationDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ApplicationDTO applicationDTO = null;


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

    public int findApplicationId(int studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ApplicationDTO applicationDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT application_id FROM application WHERE student_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                applicationDTO = new ApplicationDTO();
                applicationDTO.setApplicationId(rs.getInt("application_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return applicationDTO.getApplicationId();
    }

    public boolean applyAdmission(ApplicationDTO applicationDTO) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            // room_number,room_type, capacity, 일단 제외
            String sql = "INSERT INTO application (student_id,application_date) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationDTO.getStudentId());
            pstmt.setString(2, applicationDTO.getApplicationDate());


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

    private void closeResources(Connection conn, PreparedStatement pstmt) {
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
