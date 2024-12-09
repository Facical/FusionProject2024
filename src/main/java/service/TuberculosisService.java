package service;

import dao.TuberculosisDAO;
import dto.TuberculosisDTO;
import dto.AdmissionDTO;
import dao.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class TuberculosisService {
    private final TuberculosisDAO tuberculosisDAO = new TuberculosisDAO();
    private final AdmissionDAO admissionDAO = new AdmissionDAO();
    private final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한


    public String submitCertificate(TuberculosisDTO certificate) {
        // 파일 크기 검증
        if (certificate.getImageData().length > MAX_FILE_SIZE) {
            return "파일 크기가 너무 큽니다 (최대 10MB)";
        }

        // 파일 타입 검증
        String fileType = certificate.getFileType().toLowerCase();
        if (!fileType.equals("jpg") && !fileType.equals("jpeg") && !fileType.equals("png")) {
            return "지원되지 않는 파일 형식입니다 (jpg, jpeg, png만 가능)";
        }

        certificate.setSubmissionDate(new Date(System.currentTimeMillis()));

        // 트랜잭션으로 처리
        Connection conn = null;
        try {
            conn = tuberculosisDAO.getConnection();
            conn.setAutoCommit(false);

            // 결핵진단서 정보 저장
            boolean certificateResult = tuberculosisDAO.submitCertificate(certificate);

            // 입사 상태 업데이트
            boolean admissionResult = admissionDAO.updateCertificateStatus(certificate.getStudentId());

            if (certificateResult && admissionResult) {
                conn.commit();
                return "성공";
            } else {
                conn.rollback();
                return "실패";
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "실패";
        } finally {
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
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
}