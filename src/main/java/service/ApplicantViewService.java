package service;

import dto.ApplicantInfoDTO;

import java.util.List;

public class ApplicantViewService {

    // 비즈니스 로직 -> 출력하기 좋은 형태로 만든 후 리턴해줌
    public static String ListToString(List<ApplicantInfoDTO> applicantInfoDTOList) {
        StringBuilder sb = new StringBuilder();
        for (ApplicantInfoDTO student : applicantInfoDTOList) {
            sb.append(student.getStudentName()).append(": ")
                    .append("First Dormitory = ").append(student.getFirstDormitory()).append(", ")
                    .append("Second Dormitory = ").append(student.getSecondDormitory()).append("\n");
        }
        return sb.toString();
    }
}