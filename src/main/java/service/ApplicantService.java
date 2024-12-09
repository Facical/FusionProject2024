package service;

import dao.*;
import dto.*;

import java.util.List;

public class ApplicantService {
    private final ApplicantDAO applicantDAO;
    private final ScoreCalculator scoreCalculator;

    public ApplicantService() {
        this.applicantDAO = new ApplicantDAO();
        this.scoreCalculator = new ScoreCalculator();
    }

    public List<ApplicantDTO> getApplicantsWithScores() {
        // DAO를 통해 신청자 데이터 조회
        List<ApplicantDTO> applicants = applicantDAO.getApplicantsWithPreferences();

        // 각 신청자의 점수 계산
        for (ApplicantDTO applicant : applicants) {
            double score = scoreCalculator.calculateScore(
                    applicant.getPreviousGrade(),
                    applicant.getHomeAddress(),
                    applicant.getStudentType()
            );
            applicant.setScore(score); // 점수 설정
        }

        return applicants;
    }
}

