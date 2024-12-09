package service;

import dao.TuberculosisDAO;
import dto.TuberculosisDTO;

import java.sql.Date;
import java.util.List;
//dㅇㅇㅇ2
public class TuberculosisService {
    private final TuberculosisDAO tuberculosisDAO = new TuberculosisDAO();
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

        boolean result = tuberculosisDAO.submitCertificate(certificate);
        return result ? "성공" : "실패";
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