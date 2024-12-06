package dao;

import dto.MealDTO;
import dto.RoomDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomDAO {
    private final DataSource ds = PooledDataSource.getDataSource();


    public boolean registerRoom(RoomDTO roomDTO) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO room (dormitory_id, room_number,room_type, capacity,fee) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, roomDTO.getDormitoryId());
            pstmt.setString(2, roomDTO.getRoomNumber());
            pstmt.setString(3, roomDTO.getRoomType());
            pstmt.setInt(4, roomDTO.getDormitoryId());
            pstmt.setInt(5, roomDTO.getFee());

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
