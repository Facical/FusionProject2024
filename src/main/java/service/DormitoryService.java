package service;

import dao.DormitoryDAO;
import dto.DormitoryDTO;

// dormitoey서비스에 DAO에 관한 의존성을 주입하기 위함.
public class DormitoryService {
    private final DormitoryDAO dormitoryDAO = new DormitoryDAO();

    public DormitoryDTO findDormitoryId(){
        return dormitoryDAO.findDormitoryId();
    }

}
