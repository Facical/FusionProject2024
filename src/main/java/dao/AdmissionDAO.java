package dao;

import dto.AdmissionDTO;

import javax.sql.DataSource;
import java.sql.*;

public class AdmissionDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

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

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null)  rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
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

}
