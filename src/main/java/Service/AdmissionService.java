package Service;

import dao.AdmissionDAO;
import dao.ApplicationPreferenceDAO;
import dto.AdmissionDTO;
import dto.ApplicationDTO;
import dto.ApplicationPreferenceDTO;
import dto.StudentDTO;

public class AdmissionService {
    private final AdmissionDAO admissionDAO = new AdmissionDAO();

    public boolean findCheckAdmission(){
        return admissionDAO.findCheckAdmission();
    }

}
