package dao;

import dto.AdmissionDTO;
import dto.PaidStudentInfoDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdmissionDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public AdmissionDTO findAdmission(int studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdmissionDTO admission = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM admission WHERE student_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                admission = new AdmissionDTO();
                admission.setAdmissionId(rs.getInt("admission_id"));
                admission.setApplicationId(rs.getInt("application_id"));
                admission.setRoomId(rs.getInt("room_id"));
                admission.setBedNumber(rs.getInt("bed_number"));

                Date admissionDate = rs.getDate("admission_date");
                if (admissionDate != null) {
                    admission.setAdmissionDate(admissionDate.toLocalDate());
                }

                Date startDate = rs.getDate("residence_start_date");
                if (startDate != null) {
                    admission.setResidenceStartDate(startDate.toLocalDate());
                }

                Date endDate = rs.getDate("residence_end_date");
                if (endDate != null) {
                    admission.setResidenceEndDate(endDate.toLocalDate());
                }

                admission.setAdmissionStatus(rs.getString("admission_status"));
                admission.setCertificateStatus(rs.getString("certificate_status"));
                admission.setPaymentStatus(rs.getString("payment_status"));
                admission.setStudentId(rs.getInt("student_id")); // student_id 추가
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return admission;
    }

    // 납부 완료자 명단 조회용 메서드 추가
    public List<PaidStudentInfoDTO> findPaidStudents() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PaidStudentInfoDTO> paidList = new ArrayList<>();

        try {
            conn = ds.getConnection();
            // payment_status가 '납부 완료'인 학생, 생활관 명단 조회
            String sql = "SELECT s.name AS student_name, d.name AS dorm_name " +
                    "FROM admission a " +
                    "JOIN student s ON a.student_id = s.student_id " +
                    "JOIN room r ON a.room_id = r.room_id " +
                    "JOIN dormitory d ON r.dormitory_id = d.dormitory_id " +
                    "WHERE a.payment_status = '납부 완료'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                PaidStudentInfoDTO info = new PaidStudentInfoDTO();
                info.setStudentName(rs.getString("student_name"));
                info.setDormitoryName(rs.getString("dorm_name"));
                paidList.add(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return paidList;
    }

    public List<PaidStudentInfoDTO> findUnpaidStudents() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PaidStudentInfoDTO> unpaidList = new ArrayList<>();

        try {
            conn = ds.getConnection();
            // payment_status가 '납부 완료'인 학생, 생활관 명단 조회
            String sql = "SELECT s.name AS student_name, d.name AS dorm_name " +
                    "FROM admission a " +
                    "JOIN student s ON a.student_id = s.student_id " +
                    "JOIN room r ON a.room_id = r.room_id " +
                    "JOIN dormitory d ON r.dormitory_id = d.dormitory_id " +
                    "WHERE a.payment_status = '미납부'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                PaidStudentInfoDTO info = new PaidStudentInfoDTO();
                info.setStudentName(rs.getString("student_name"));
                info.setDormitoryName(rs.getString("dorm_name"));
                unpaidList.add(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return unpaidList;
    }

    public void insertAdmission(AdmissionDTO admission) {
        String sql = "INSERT INTO admission(application_id, student_id, room_id, bed_number, admission_status, certificate_status, payment_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, admission.getApplicationId());
            pstmt.setInt(2, admission.getStudentId());
            pstmt.setInt(3, admission.getRoomId());
            pstmt.setInt(4, admission.getBedNumber());
            pstmt.setString(5, admission.getAdmissionStatus());
            pstmt.setString(6, admission.getCertificateStatus());
            pstmt.setString(7, admission.getPaymentStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
