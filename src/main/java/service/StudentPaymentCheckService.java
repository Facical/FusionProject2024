package service;

import dto.StudentPaymentDTO;


import java.util.List;
//dㅇㅇㅇㅇ
public class StudentPaymentCheckService {

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
