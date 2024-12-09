package dao;

import dto.AdmissionDTO;

import javax.sql.DataSource;
import java.sql.*;

public class AdmissionDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public void saveAdmission(AdmissionDTO admission) {
        String sql = "INSERT INTO admission " +
                "(application_id, dormitory_id, room_id, bed_number, admission_date, residence_start_date, residence_end_date, " +
                " admission_status, certificate_status, payment_status, student_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, admission.getApplicationId());
            pstmt.setInt(2, admission.getDormitoryId());
            pstmt.setInt(3, admission.getRoomId());
            pstmt.setInt(4, admission.getBedNumber());
            pstmt.setDate(5, java.sql.Date.valueOf(admission.getAdmissionDate()));
            pstmt.setDate(6, java.sql.Date.valueOf(admission.getResidenceStartDate()));
            pstmt.setDate(7, java.sql.Date.valueOf(admission.getResidenceEndDate()));
            pstmt.setString(8, admission.getAdmissionStatus());
            pstmt.setString(9, admission.getCertificateStatus());
            pstmt.setString(10, admission.getPaymentStatus());
            pstmt.setInt(11, admission.getStudentId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public AdmissionDTO findAdmission(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdmissionDTO admission = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM admission WHERE student_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            // 합격 시에는 방정보도 찍어줘야 하는데...
            // 질의를 한 번 더 해서 admission table의 room id = room table의 room id 에서 room dto에 넣어주고
            // return 할 때 admissionDTO가 아니고 admissionAndRoomDTO 필요한 정보들 다 박아주고 얘를 return
            // 받은 거를 메시지화 해주고, 패킷화 해주고, 전송
            if (rs.next()) {
                admission = new AdmissionDTO();
                admission.setAdmissionId(rs.getInt("admission_id"));
                admission.setApplicationId(rs.getInt("application_id"));
                admission.setRoomId(rs.getInt("room_id"));
                admission.setBedNumber(rs.getInt("bed_number"));
                admission.setDormitoryId(rs.getInt("dormitory_id"));

                // 날짜 타입은 ResultSet에서 java.sql.Date로 받아온 뒤 LocalDate로 변환
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return admission;
    }

    public void UpdatePaymentStatus(AdmissionDTO admissionDTO){
        Connection conn = null;
        PreparedStatement pstmt = null;
        int updateResult;

        try {
            conn = ds.getConnection();
            String sql = "UPDATE admission SET payment_status = ? WHERE admission_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, admissionDTO.getPaymentStatus());
            pstmt.setInt(2, admissionDTO.getAdmissionId());
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

    public boolean updateCertificateStatus(int studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "UPDATE admission SET certificate_status = '완료' WHERE student_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, null);
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

/*    // student id를 기준으로 student table student_id = admission table student_id ->
    public boolean findCheckAdmission(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdmissionDTO admissionDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM admission WHERE application_id = ? AND room_id = ? AND admission_status = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, admissionDTO.getApplicationId());
            pstmt.setInt(2, admissionDTO.getRoomId());
            pstmt.setString(3, admissionDTO.getAdmissionStatus());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                admissionDTO = new AdmissionDTO();
                admissionDTO.setApplicationId(rs.getInt("application_id"));
                admissionDTO.setRoomId(rs.getInt("room_id"));
                admissionDTO.setAdmissionStatus(rs.getString("admission_status"));
            }
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }

    }

    public AdmissionDTO findCheckAdmission() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdmissionDTO admissionDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM admission WHERE application_id = ? AND room_id = ? AND admission_status = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, admissionDTO.getApplicationId());
            pstmt.setInt(2, admissionDTO.getRoomId());
            pstmt.setString(3, admissionDTO.getAdmissionStatus());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                admissionDTO.setApplicationId(rs.getInt("application_id"));
                admissionDTO.setRoomId(rs.getInt("room_id"));
                admissionDTO.setAdmissionStatus(rs.getString("admission_status"));
            }
            int result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            closeResources(conn, pstmt, rs);
        }

    }*/

    // Insert, Update, Delete 메서드를 추가할 수 있음.
    // 예: insertAdmission(), updateAdmission(), deleteAdmission() 등.








