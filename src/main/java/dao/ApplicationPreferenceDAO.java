package dao;

import dto.ApplicationDTO;
import dto.ApplicationPreferenceDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplicationPreferenceDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

        Connection conn = null;
        PreparedStatement pstmt = null;

    //  DB로 부터 Application_Preference 정보를 가져오는 함수
    public ApplicationPreferenceDTO getApplicationPreference(int application_id, int dormitory_id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ApplicationPreferenceDTO applicationPreferenceDTO = null;

        try {
            System.out.println("test");
            System.out.println(application_id + "," + dormitory_id);
            conn = ds.getConnection();
            String sql = "SELECT * FROM application_preference WHERE application_id = ? AND dormitory_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, application_id);
            pstmt.setInt(2, dormitory_id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                applicationPreferenceDTO = new ApplicationPreferenceDTO();
                applicationPreferenceDTO.setApplication_preference_id(rs.getInt("application_preference_id"));
                applicationPreferenceDTO.setApplication_id(rs.getInt("application_id"));
                applicationPreferenceDTO.setDormitory_id(rs.getInt("dormitory_id"));
                applicationPreferenceDTO.setMeal_id(rs.getInt("meal_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return applicationPreferenceDTO;
    }

    // 학생이 입사 신청시 기록한 기숙사 지망과 식사 정보를 DB에 업데이트하는 함수
    public boolean applyPreference(ApplicationPreferenceDTO applicationPreferenceDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO application_preference (application_id,dormitory_id,preference_order,meal_id) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationPreferenceDTO.getApplication_id());
            pstmt.setInt(2, applicationPreferenceDTO.getDormitory_id());
            pstmt.setInt(3, applicationPreferenceDTO.getPreference_order());
            pstmt.setInt(4, applicationPreferenceDTO.getMeal_id());
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
