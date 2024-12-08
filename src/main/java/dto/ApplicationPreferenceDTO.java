package dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString

public class ApplicationPreferenceDTO {
    private int application_preference_id;
    private int application_id;
    private int preference_order;
    private int dormitory_id;
    private int meal_id;
}
