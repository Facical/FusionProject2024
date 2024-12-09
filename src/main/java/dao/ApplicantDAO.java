package dao;

import dto.ApplicantDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicantDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public List<ApplicantDTO> getApplicantsWithPreferences() {
        List<ApplicantDTO> applicants = new ArrayList<>();
        String sql = "SELECT " +
                "    s.student_id, s.name, s.gender, s.home_address, s.previous_grade, s.student_type, " +
                "    a.application_id, " +
                "    ap1.dormitory_id AS first_dormitory_id, d1.name AS first_dormitory_name, m1.meal_id AS first_meal_id, m1.name AS first_meal_name, " +
                "    ap2.dormitory_id AS second_dormitory_id, d2.name AS second_dormitory_name, m2.meal_id AS second_meal_id, m2.name AS second_meal_name " +
                "FROM student s " +
                "JOIN application a ON s.student_id = a.student_id " +
                "JOIN application_preference ap1 ON a.application_id = ap1.application_id AND ap1.preference_order = 1 " +
                "LEFT JOIN dormitory d1 ON ap1.dormitory_id = d1.dormitory_id " +
                "LEFT JOIN meal m1 ON ap1.meal_id = m1.meal_id " +
                "JOIN application_preference ap2 ON a.application_id = ap2.application_id AND ap2.preference_order = 2 " +
                "LEFT JOIN dormitory d2 ON ap2.dormitory_id = d2.dormitory_id " +
                "LEFT JOIN meal m2 ON ap2.meal_id = m2.meal_id";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ApplicantDTO applicant = new ApplicantDTO();
                // 기본 정보
                applicant.setStudentId(rs.getInt("student_id"));
                applicant.setName(rs.getString("name"));
                applicant.setGender(rs.getString("gender"));
                applicant.setHomeAddress(rs.getString("home_address"));
                applicant.setPreviousGrade(rs.getDouble("previous_grade"));
                applicant.setStudentType(rs.getString("student_type"));

                // Application ID
                applicant.setApplicationId(rs.getInt("application_id"));

                // 1지망
                applicant.setFirstDormitoryId(rs.getInt("first_dormitory_id"));
                applicant.setFirstDormitoryName(rs.getString("first_dormitory_name"));
                applicant.setFirstMealId(rs.getInt("first_meal_id"));
                applicant.setFirstMealName(rs.getString("first_meal_name"));

                // 2지망
                applicant.setSecondDormitoryId(rs.getInt("second_dormitory_id"));
                applicant.setSecondDormitoryName(rs.getString("second_dormitory_name"));
                applicant.setSecondMealId(rs.getInt("second_meal_id"));
                applicant.setSecondMealName(rs.getString("second_meal_name"));

                applicants.add(applicant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applicants;
    }
}
