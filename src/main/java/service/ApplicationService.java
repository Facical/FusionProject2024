package service;

import dao.ApplicationDAO;
import dto.ApplicationDTO;

// application서비스에 DAO에 관한 의존성을 주입하기 위함.
public class ApplicationService {
    private final ApplicationDAO applicationDAO = new ApplicationDAO();

    public boolean applyAdmission(ApplicationDTO applicationDTO){
        return applicationDAO.applyAdmission(applicationDTO);
    }
    public int findApplicationId(int studentId){
        return applicationDAO.findApplicationId(studentId);
    }
}
