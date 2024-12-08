package service;

import dao.AdmissionDAO;
import dto.AdmissionDTO;
import dto.PaidStudentInfoDTO;
import java.util.List;

public class CheckPaymentStatusService {
    private AdmissionDTO admissionDTO;
    private AdmissionDAO admissionDAO;

    public boolean checkPaymentStatus(int studentId) {
        admissionDAO = new AdmissionDAO();
        admissionDTO = admissionDAO.findAdmission(studentId);

        if (admissionDTO == null) {
            // 해당 학번의 정보가 없음
            return false;
        }

        String status = admissionDTO.getPaymentStatus();
        return "납부 완료".equals(status);
    }

    // 새로 구현한 납부자 명단 조회 메서드
    public void printPaidStudentList() {
        admissionDAO = new AdmissionDAO();
        List<PaidStudentInfoDTO> paidList = admissionDAO.findPaidStudents();

        System.out.println("=== 납부 완료 학생 명단 ===");
        for (PaidStudentInfoDTO info : paidList) {
            System.out.println("학생 이름: " + info.getStudentName() + ", 생활관: " + info.getDormitoryName());
        }
    }

    public void printUnpaidStudentList() {
        admissionDAO = new AdmissionDAO();
        List<PaidStudentInfoDTO> paidList = admissionDAO.findUnpaidStudents();

        System.out.println("=== 미납부 학생 명단 ===");
        for (PaidStudentInfoDTO info : paidList) {
            System.out.println("학생 이름: " + info.getStudentName() + ", 생활관: " + info.getDormitoryName());
        }
    }
}
