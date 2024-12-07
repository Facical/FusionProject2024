package dao;

import dto.TuberculosisDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TuberculosisDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public boolean submitCertificate(TuberculosisDTO certificate) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO tuberculosis_certificate (certification_id, student_id, submission_date, due_date, image_data, file_name, file_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, certificate.getCertificationId());
            pstmt.setInt(2, certificate.getStudentId());
            pstmt.setDate(3, certificate.getSubmissionDate());
            pstmt.setDate(4, certificate.getDueDate());
            pstmt.setBytes(5, certificate.getImageData());
            pstmt.setString(6, certificate.getFileName());
            pstmt.setString(7, certificate.getFileType());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    public List<TuberculosisDTO> getCertificates() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TuberculosisDTO> certificates = new ArrayList<>();

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM tuberculosis_certificate";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TuberculosisDTO certificate = new TuberculosisDTO();
                certificate.setCertificationId(rs.getInt("certification_id"));
                certificate.setStudentId(rs.getInt("student_id"));
                certificate.setSubmissionDate(rs.getDate("submission_date"));
                certificate.setDueDate(rs.getDate("due_date"));
                certificate.setImageData(rs.getBytes("image_data"));
                certificate.setFileName(rs.getString("file_name"));
                certificate.setFileType(rs.getString("file_type"));
                certificates.add(certificate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return certificates;
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