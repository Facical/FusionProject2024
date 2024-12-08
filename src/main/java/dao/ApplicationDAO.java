package dao;

import dto.ApplicantInfoDTO;
import dto.ApplicationDTO;
import dto.ApplicationPreferenceDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {
    private final DataSource ds = PooledDataSource.getDataSource();
    private final ApplicationPreferenceDAO preferenceDAO = new ApplicationPreferenceDAO();


    // 모든 신청자 명단 조회: student와 application 조인
    public List<ApplicantInfoDTO> findAllApplicants() {
        List<ApplicantInfoDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ds.getConnection();
            // 예시 쿼리: student.name, application.application_date
            String sql = "SELECT s.name AS student_name, a.application_date " +
                    "FROM application a " +
                    "JOIN student s ON a.student_id = s.student_id " +
                    "ORDER BY a.application_date ASC"; // 신청일자 순서로 정렬(선택사항)
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ApplicantInfoDTO dto = new ApplicantInfoDTO();
                dto.setStudentName(rs.getString("student_name"));
                Date appDate = rs.getDate("application_date");
                if (appDate != null) {
                    dto.setApplicationDate(appDate.toLocalDate());
                }
                list.add(dto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return list;
    }

    // 관별 신청자 명단 조회 메서드
    public List<ApplicantInfoDTO> findApplicantsByDorm(int dormitoryId) {
        List<ApplicantInfoDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ds.getConnection();
            // dormitory_id로 필터링하여 student, dormitory 조인
            String sql = "SELECT s.name AS student_name, a.application_date, d.name AS dorm_name " +
                    "FROM application a " +
                    "JOIN student s ON a.student_id = s.student_id " +
                    "JOIN dormitory d ON a.dormitory_id = d.dormitory_id " +
                    "WHERE d.dormitory_id = ? " +
                    "ORDER BY a.application_date ASC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dormitoryId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ApplicantInfoDTO dto = new ApplicantInfoDTO();
                dto.setStudentName(rs.getString("student_name"));
                Date appDate = rs.getDate("application_date");
                if (appDate != null) {
                    dto.setApplicationDate(appDate.toLocalDate());
                }
                dto.setDormitoryName(rs.getString("dorm_name"));
                list.add(dto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return list;
    }

    /*public List<ApplicationDTO> findAllApplications() {
        List<ApplicationDTO> apps = new ArrayList<>();
        String sql = "SELECT * FROM application";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ApplicationDTO a = new ApplicationDTO();
                a.setApplicationId(rs.getInt("application_id"));
                a.setStudentId(rs.getInt("student_id"));
                Date d = rs.getDate("application_date");
                if (d != null) a.setApplicationDate(d.toLocalDate());
                a.setScheduleId(rs.getInt("schedule_id"));

                List<ApplicationPreferenceDTO> prefList = preferenceDAO.findPreferencesByApplicationId(a.getApplicationId());
                a.setPreferenceOrder(prefList);

                apps.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }*/

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null)  rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
