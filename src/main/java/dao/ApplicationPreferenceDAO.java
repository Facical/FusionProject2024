package dao;

import dto.ApplicationPreferenceDTO;
import javax.sql.DataSource;
import java.sql.*;

public class ApplicationPreferenceDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

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
                applicationPreferenceDTO.setPreference_order(rs.getInt("preference_order"));
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
