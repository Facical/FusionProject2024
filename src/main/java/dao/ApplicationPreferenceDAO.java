package dao;

import dto.ApplicationPreferenceDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationPreferenceDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    // 특정 application_id에 해당하는 모든 선호도 정보 조회
    public List<ApplicationPreferenceDTO> findPreferencesByApplicationId(int applicationID) {
        List<ApplicationPreferenceDTO> preferences = new ArrayList<>();

        String sql = "SELECT application_preference_id, application_id, preference_order, dormitory_id, meal_id " +
                "FROM application_preference " +
                "WHERE application_id = ? " +
                "ORDER BY preference_order ASC";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, applicationID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ApplicationPreferenceDTO pref = new ApplicationPreferenceDTO();
                    pref.setApplicationPreferenceID(rs.getInt("application_preference_id"));
                    pref.setApplicationID(rs.getInt("application_id"));
                    pref.setPreferenceOrder(rs.getInt("preference_order"));
                    pref.setDormitoryID(rs.getInt("dormitory_id"));
                    pref.setMealID(rs.getInt("meal_id"));

                    preferences.add(pref);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return preferences;
    }

    // 모든 application_preference 조회 (필요시)
    public List<ApplicationPreferenceDTO> findAllPreferences() {
        List<ApplicationPreferenceDTO> list = new ArrayList<>();
        String sql = "SELECT application_preference_id, application_id, preference_order, dormitory_id, meal_id " +
                "FROM application_preference " +
                "ORDER BY application_id, preference_order ASC";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ApplicationPreferenceDTO pref = new ApplicationPreferenceDTO();
                pref.setApplicationPreferenceID(rs.getInt("application_preference_id"));
                pref.setApplicationID(rs.getInt("application_id"));
                pref.setPreferenceOrder(rs.getInt("preference_order"));
                pref.setDormitoryID(rs.getInt("dormitory_id"));
                pref.setMealID(rs.getInt("meal_id"));
                list.add(pref);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 선호도 정보 삽입 (필요시)
    public int insertPreference(ApplicationPreferenceDTO preference) {
        String sql = "INSERT INTO application_preference(application_preference_id, application_id, preference_order, dormitory_id, meal_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, preference.getApplicationPreferenceID());
            pstmt.setInt(2, preference.getApplicationID());
            pstmt.setInt(3, preference.getPreferenceOrder());
            pstmt.setInt(4, preference.getDormitoryID());
            pstmt.setInt(5, preference.getMealID());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 업데이트, 삭제 메서드도 필요하다면 추가
    // public int updatePreference(...){...}
    // public int deletePreference(...){...}
}
