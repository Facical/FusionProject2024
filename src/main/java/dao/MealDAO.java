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
    public MealDTO getMealInfo(int dormitory_id) {
        /*
        생활관 아이디를 기준으로 환불 정보를 가져오는 함수
         */
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MealDTO mealDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM meal WHERE dormitory_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dormitory_id);
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

    public boolean registerMeal(MealDTO mealDTO) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO meal (dormitory_id, fee) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, mealDTO.getDormitoryId());
            pstmt.setInt(2, mealDTO.getFee());


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
