package service;

import dao.MealDAO;
import dto.MealDTO;

public class MealService {
    private final MealDAO mealDAO = new MealDAO();

    public int getMealId(int dormitoryId, String mealName){
        return mealDAO.getMealId(dormitoryId, mealName);
    }
    public boolean registerMeal(MealDTO mealDTO){
        return mealDAO.registerMeal(mealDTO);
    }
}
