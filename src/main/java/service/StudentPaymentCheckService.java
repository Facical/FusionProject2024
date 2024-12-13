package service;

import dto.StudentPaymentDTO;
import dto.TuberculosisDTO;

import java.util.List;

public class StudentPaymentCheckService {

    // 스트링으로 만들어서 return 해주는 용도
    public static String ListToString(List<StudentPaymentDTO> studentPaymentDTOList)
    {
        StringBuilder sb = new StringBuilder();
        for (StudentPaymentDTO student : studentPaymentDTOList) {
            sb.append(student.getDormitoryName()).append(" | ")
                    .append(student.getStudentName()).append(",");
        }

        return sb.toString();
    }

}
