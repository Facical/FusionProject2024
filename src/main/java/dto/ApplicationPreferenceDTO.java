package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationPreferenceDTO {
    private int application_preference_id;
    private int application_id;
    private int preference_first;
    private int preference_second;
    private int dormitory_id;
    private String meal_first;
    private String meal_second;
    private int meal_id;
}
