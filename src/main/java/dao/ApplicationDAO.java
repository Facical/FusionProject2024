package dao;

import dto.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//APPLY_ADMISSION
public class ApplicationDAO {
    private final DataSource ds = PooledDataSource.getDataSource();


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
                applicationDTO.setApplication_id(rs.getInt("application_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return applicationDTO.getApplication_id();
    }

    public boolean applyAdmission(ApplicationDTO applicationDTO) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            // room_number,room_type, capacity, 일단 제외
            String sql = "INSERT INTO application (student_id,application_date) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationDTO.getStudent_id());
            pstmt.setString(2, applicationDTO.getApplication_date());


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
