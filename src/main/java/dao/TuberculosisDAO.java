// 데이터베이스 접근을 담당하는 DAO 클래스
package dao;

import dto.TuberculosisDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TuberculosisDAO {
    // Connection Pool에서 데이터소스 가져오기
    private final DataSource ds = PooledDataSource.getDataSource();

    // 데이터베이스 연결 객체 반환
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    // 결핵진단서 제출 메서드
    public boolean submitCertificate(TuberculosisDTO certificate) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            // 결핵진단서 정보를 데이터베이스에 삽입하는 SQL
            String sql = "INSERT INTO tuberculosis_certificate (student_id, submission_date, due_date, image_data, file_name, file_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            // PreparedStatement에 파라미터 설정
            pstmt.setInt(1, certificate.getStudentId());           // 학번
            pstmt.setDate(2, certificate.getSubmissionDate());     // 제출일
            pstmt.setDate(3, certificate.getDueDate());           // 제출기한
            pstmt.setBytes(4, certificate.getImageData());        // 이미지 데이터
            pstmt.setString(5, certificate.getFileName());        // 파일명
            pstmt.setString(6, certificate.getFileType());        // 파일 타입

            // SQL 실행 및 결과 확인 (영향받은 행이 1개 이상이면 true)
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    // 모든 결핵진단서 조회 메서드
    public List<TuberculosisDTO> getCertificates() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TuberculosisDTO> certificates = new ArrayList<>();

        try {
            conn = ds.getConnection();
            // 모든 진단서를 제출일 기준 내림차순으로 조회
            String sql = "SELECT * FROM tuberculosis_certificate ORDER BY submission_date DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // 조회 결과를 DTO 객체로 변환하여 리스트에 추가
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

    // 데이터베이스 리소스 정리 메서드
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();        // ResultSet 닫기
            if (pstmt != null) pstmt.close();  // PreparedStatement 닫기
            if (conn != null) conn.close();     // Connection 반환(커넥션 풀로)
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}