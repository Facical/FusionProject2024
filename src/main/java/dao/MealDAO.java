package dao;

import dto.MealDTO;
import dto.ScheduleDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

public class MealDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    // public MealDTO getMealInfo(int dormitory_id) {
    public MealDTO getMealInfo(int meal_id) {
        /*
        생활관 아이디를 기준으로 환불 정보를 가져오는 함수
         */
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MealDTO mealDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM meal WHERE meal_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, meal_id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                mealDTO = new MealDTO();
                mealDTO.setMealId(rs.getInt("meal_id"));
                mealDTO.setDormitoryId(rs.getInt("dormitory_id"));
                mealDTO.setName(rs.getString("name"));
                mealDTO.setFee(rs.getInt("fee"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return mealDTO;
    }
    // DB에 MEAL 테이블에서 mealID를 가져오기 위한 과정.
    public int getMealId(int dormitoryId, String mealName){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int mealId = -1;
        try{
            conn = ds.getConnection();
            String sql = "SELECT meal_id FROM meal WHERE dormitory_id = ? AND name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dormitoryId);
            pstmt.setString(2, mealName);
            rs = pstmt.executeQuery();
            if(rs.next()){
                mealId = rs.getInt("meal_id");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }finally {
            closeResources(conn, pstmt, rs);
        }
        return mealId;
    }
    // mealDTO 정보를 이용해 DB에 meal 테이블에 INSERT 하는 과정.
    public boolean registerMeal(MealDTO mealDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO meal (dormitory_id, name ,fee) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, mealDTO.getDormitoryId());
            pstmt.setString(2, mealDTO.getName());
            pstmt.setInt(3, mealDTO.getFee());


            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }


    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
