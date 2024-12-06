package dao;

import dto.MealDTO;
import dto.ScheduleDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MealDAO {
    private final DataSource ds = PooledDataSource.getDataSource();


    public boolean registerMeal(MealDTO mealDTO) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO meal (dormitory_id, name, fee) VALUES (?, ?, ?)";
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
