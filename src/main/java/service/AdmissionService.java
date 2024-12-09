package service;

import dao.AdmissionDAO;
import dao.ApplicationPreferenceDAO;
import dto.AdmissionDTO;
import dto.ApplicationDTO;
import dto.ApplicationPreferenceDTO;
import dto.StudentDTO;

public class AdmissionService {
    private final AdmissionDAO admissionDAO = new AdmissionDAO();

    public AdmissionDTO findAdmission(int id){
        return admissionDAO.findAdmission(id);
    }

    //public boolean findCheckAdmission(){return admissionDAO.findCheckAdmission();}

}
