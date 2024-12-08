package service;

import dao.ApplicationDAO;
import dto.ApplicantInfoDTO;

import java.util.List;

public class CheckApplicantService {
    private ApplicationDAO applicationDAO;

    public CheckApplicantService() {
        applicationDAO = new ApplicationDAO();
    }

    // 특정 생활관 관별 신청자 명단 출력
    public void printApplicantsByDorm(int dormitoryId) {
        List<ApplicantInfoDTO> applicants = applicationDAO.findApplicantsByDorm(dormitoryId);
        System.out.println("=== " + dormitoryId + "번 생활관 신청자 명단 ===");
        for (ApplicantInfoDTO info : applicants) {
            System.out.println("학생 이름: " + info.getStudentName() +
                    ", 신청일자: " + info.getApplicationDate() +
                    ", 생활관: " + info.getDormitoryName());
        }
    }
}
