package service;

import dao.TuberculosisDAO;
import dto.TuberculosisDTO;
import dao.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class TuberculosisService {
    // DAO 객체 선언
    private final TuberculosisDAO tuberculosisDAO = new TuberculosisDAO();
    private final AdmissionDAO admissionDAO = new AdmissionDAO();

    // 최대 허용 파일 크기 상수 (10MB)
    private final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한


    public String submitCertificate(TuberculosisDTO certificate) {
        // 파일 크기 검증 (10MB 초과 불가)
        if (certificate.getImageData().length > MAX_FILE_SIZE) {
            return "파일 크기가 너무 큽니다 (최대 10MB)";
        }

        // 파일 형식 검증 (jpg, jpeg, png만 허용)
        String fileType = certificate.getFileType().toLowerCase();
        if (!fileType.equals("jpg") && !fileType.equals("jpeg") && !fileType.equals("png")) {
            return "지원되지 않는 파일 형식입니다 (jpg, jpeg, png만 가능)";
        }

        // 현재 시간을 제출 시간으로 설정
        certificate.setSubmissionDate(new Date(System.currentTimeMillis()));

        // 트랜잭션 처리를 위한 Connection 관리
        Connection conn = null;
        try {
            conn = tuberculosisDAO.getConnection();
            conn.setAutoCommit(false);

            // 1. 결핵진단서 정보를 데이터베이스에 저장
            boolean certificateResult = tuberculosisDAO.submitCertificate(certificate);

            // 2. 입사 상태 업데이트 (제출 완료로 변경)
            boolean admissionResult = admissionDAO.updateCertificateStatus(certificate.getStudentId());

            // 두 작업 모두 성공한 경우에만 커밋
            if (certificateResult && admissionResult) {
                conn.commit();
                return "성공";
            } else {
                conn.rollback();
                return "실패";
            }
        } catch (SQLException e) {
            // SQL 예외 발생 시 롤백 처리
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "실패";
        } finally {
            // 리소스 정리
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public List<TuberculosisDTO> getCertificates() {
        return tuberculosisDAO.getCertificates();
    }

    public String formatCertificateList(List<TuberculosisDTO> certificates) {
        StringBuilder sb = new StringBuilder();
        for (TuberculosisDTO cert : certificates) {
            sb.append(cert.getStudentId()).append(",")
                    .append(cert.getSubmissionDate()).append(",")
                    .append(cert.getDueDate()).append(";");
        }
        // 마지막 세미콜론 제거 후 반환
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
}