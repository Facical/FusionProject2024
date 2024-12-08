package Service;

import dao.StudentDAO;
import dto.StudentDTO;

import java.util.List;

public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();

    public List<StudentDTO> getAllStudentInfo(){
        return studentDAO.getAllStudentInfo();
    }
}
