package service;

import dao.MealDAO;
import dto.MealDTO;

//ddddㅇㅇddㅇㅇdㅇdddㅇ2
public class MealService {
    private final MealDAO mealDAO = new MealDAO();

//    public MealDTO getMealInfo(int dormitory_id){
//        return mealDAO.getMealInfo(dormitory_id);
//    }
    public boolean registerMeal(MealDTO mealDTO){
        return mealDAO.registerMeal(mealDTO);
    }
    public int getMealId(int dormitoryId, String mealName){
        return mealDAO.getMealId(dormitoryId, mealName);
    }
}
