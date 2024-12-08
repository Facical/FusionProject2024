package dao;

import dto.ApplicantInfoDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicantInfoDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public List<ApplicantInfoDTO> getApplicantDormitoryInfo() {
        List<ApplicantInfoDTO> applicantList = new ArrayList<>();
        String sql = "SELECT s.name AS student_name, " +
                "       MAX(CASE WHEN ap.preference_order = 1 THEN d.name END) AS first_dormitory, " +
                "       MAX(CASE WHEN ap.preference_order = 2 THEN d.name END) AS second_dormitory " +
                "FROM application a " +
                "JOIN student s ON a.student_id = s.student_id " +
                "JOIN application_preference ap ON a.application_id = ap.application_id " +
                "JOIN dormitory d ON ap.dormitory_id = d.dormitory_id " +
                "GROUP BY s.name " +
                "ORDER BY s.name";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ApplicantInfoDTO applicantInfo = new ApplicantInfoDTO();
                applicantInfo.setStudentName(rs.getString("student_name"));
                applicantInfo.setFirstDormitory(rs.getString("first_dormitory"));
                applicantInfo.setSecondDormitory(rs.getString("second_dormitory"));
                applicantList.add(applicantInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applicantList;
    }
}
