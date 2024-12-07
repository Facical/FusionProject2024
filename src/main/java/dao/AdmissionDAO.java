package dao;

import dto.AdmissionDTO;

import javax.sql.DataSource;
import java.sql.*;

public class AdmissionDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public AdmissionDTO findAdmission(int admissionId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdmissionDTO admission = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM admission WHERE admission_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, admissionId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                admission = new AdmissionDTO();
                admission.setAdmissionId(rs.getInt("admission_id"));
                admission.setApplicationId(rs.getInt("application_id"));
                admission.setRoomId(rs.getInt("room_id"));
                admission.setBedNumber(rs.getInt("bed_number"));

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

    // Insert, Update, Delete 메서드를 추가할 수 있음.
    // 예: insertAdmission(), updateAdmission(), deleteAdmission() 등.

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
