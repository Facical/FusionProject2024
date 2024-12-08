package Service;

import dao.MealDAO;
import dto.MealDTO;

public class MealService {
    private final MealDAO mealDAO = new MealDAO();


    public boolean registerMeal(MealDTO mealDTO){
        return mealDAO.registerMeal(mealDTO);
    }
}
