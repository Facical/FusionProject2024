package Service;

import dao.MealDAO;
import dto.MealDTO;

public class MealService {
    private final MealDAO mealDAO;

    public MealService(MealDAO mealDAO){
        this.mealDAO = mealDAO;
    }
    public boolean registerMeal(MealDTO mealDTO){
        return mealDAO.registerMeal(mealDTO);
    }
}
