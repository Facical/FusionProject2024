package service;

import dto.ApplicantInfoDTO;

import java.util.List;
//dddddddㅇㅇ
public class ApplicantViewService {
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