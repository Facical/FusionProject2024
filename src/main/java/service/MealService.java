package service;

import dao.MealDAO;
import dto.MealDTO;

// meal서비스에 DAO에 관한 의존성을 주입하기 위함.
public class MealService {
    private final MealDAO mealDAO = new MealDAO();

    public int getMealId(int dormitoryId, String mealName){
        return mealDAO.getMealId(dormitoryId, mealName);
    }
    public boolean registerMeal(MealDTO mealDTO){
        return mealDAO.registerMeal(mealDTO);
    }
}
