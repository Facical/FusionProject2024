package Service;

import dao.DormitoryDAO;
import dto.DormitoryDTO;
import dto.StudentDTO;

import java.util.List;

public class DormitoryService {
    private final DormitoryDAO dormitoryDAO = new DormitoryDAO();

    public DormitoryDTO findDormitoryId(){
        return dormitoryDAO.findDormitoryId();
    }

}
