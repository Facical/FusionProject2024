package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MealDTO {
    private int mealId;
    private int dormitoryId;
    private String name;
    private int fee;
}
