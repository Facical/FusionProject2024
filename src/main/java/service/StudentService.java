package service;

import dao.StudentDAO;
import dto.StudentDTO;

import java.util.List;
//dㅇㅇ2
public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();

    public List<StudentDTO> getAllStudentInfo(){
        return studentDAO.getAllStudentInfo();
    }
    public String getGender(int studentId){
        return studentDAO.getGender(studentId);
    }

}
