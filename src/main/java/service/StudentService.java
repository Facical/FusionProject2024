package service;

import dao.StudentDAO;
import dto.StudentDTO;

import java.util.List;

// student서비스에 DAO에 관한 의존성을 주입하기 위함.
public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();
    public String getGender(int studentId){
        return studentDAO.getGender(studentId);
    }
    public List<StudentDTO> getAllStudentInfo(){
        return studentDAO.getAllStudentInfo();
    }
}
