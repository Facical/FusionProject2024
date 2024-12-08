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

    public ApplicationPreferenceDTO getApplicationPreference(int application_id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ApplicationPreferenceDTO applicationPreferenceDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM application_preference WHERE application_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, application_id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                applicationPreferenceDTO = new ApplicationPreferenceDTO();
                applicationPreferenceDTO.setApplication_preference_id(rs.getInt("application_preference_id"));
                applicationPreferenceDTO.setApplication_id(rs.getInt("application_id"));
                applicationPreferenceDTO.setPreference_first(rs.getInt("preference_first"));
                applicationPreferenceDTO.setPreference_second(rs.getInt("preference_second"));
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
    public boolean applyPreference(ApplicationPreferenceDTO applicationPreferenceDTO) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            //private int application_preference_id;
            //    private int application_id;
            //    private int preference_first;
            //    private int preference_second;
            //    private int dormitory_id;
            //    private int meal_first;
            //    private int meal_second;
            //    private int meal_id;
            String sql = "INSERT INTO application_preference (application_id,preference_first,preference_second,meal_first,meal_second) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationPreferenceDTO.getApplication_id());
            pstmt.setInt(2, applicationPreferenceDTO.getPreference_first());
            pstmt.setInt(3, applicationPreferenceDTO.getPreference_second());
            pstmt.setString(4, applicationPreferenceDTO.getMeal_first());
            pstmt.setString(5, applicationPreferenceDTO.getMeal_second());

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
