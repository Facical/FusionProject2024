package service;

import dao.ApplicationPreferenceDAO;
import dto.ApplicationPreferenceDTO;

// applicationPreference서비스에 DAO에 관한 의존성을 주입하기 위함.
public class ApplicationPreferenceService {
    private final ApplicationPreferenceDAO applicationPreferenceDAO = new ApplicationPreferenceDAO();

    public boolean applyPreference(ApplicationPreferenceDTO applicationPreferenceDTO){
        return applicationPreferenceDAO.applyPreference(applicationPreferenceDTO);
    }
}
