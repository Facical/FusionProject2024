package dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString

public class MealDTO {
    private int meal_id;
    private int dormitory_id;
    private String name;
    private int fee;
}
