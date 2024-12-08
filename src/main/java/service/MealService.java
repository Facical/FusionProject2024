package service;

import dao.MealDAO;
import dto.MealDTO;

//ddddㅇㅇ
public class MealService {
    private final MealDAO mealDAO = new MealDAO();

    public MealDTO getMealInfo(int dormitory_id){
        return mealDAO.getMealInfo(dormitory_id);
    }
    public boolean registerMeal(MealDTO mealDTO){
        return mealDAO.registerMeal(mealDTO);
    }
}
