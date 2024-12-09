package service;

import dao.ApplicationPreferenceDAO;
import dto.ApplicationPreferenceDTO;
//dㅇㅇ2
public class ApplicationPreferenceService {
    private final ApplicationPreferenceDAO applicationPreferenceDAO = new ApplicationPreferenceDAO();

    public boolean applyPreference(ApplicationPreferenceDTO applicationPreferenceDTO){
        return applicationPreferenceDAO.applyPreference(applicationPreferenceDTO);
    }
}
