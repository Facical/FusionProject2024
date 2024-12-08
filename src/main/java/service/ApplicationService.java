package service;

import dao.ApplicationDAO;
import dto.ApplicationDTO;


public class ApplicationService {
    private final ApplicationDAO applicationDAO = new ApplicationDAO();


    public boolean applyAdmission(ApplicationDTO applicationDTO){
        return applicationDAO.applyAdmission(applicationDTO);
    }
    public int findApplicationId(int studentId){
        return applicationDAO.findApplicationId(studentId);
    }
}
