package service;

import dao.StudentDAO;
import dto.StudentDTO;

import java.util.List;

public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();
    public String getGender(int studentId){
        return studentDAO.getGender(studentId);
    }
    public List<StudentDTO> getAllStudentInfo(){
        return studentDAO.getAllStudentInfo();
    }
}
