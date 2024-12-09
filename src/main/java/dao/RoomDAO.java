package dao;

import dto.MealDTO;
import dto.RoomDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

public class RoomDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public RoomDTO getRoomInfo(int dormitory_id){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        RoomDTO roomDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM room WHERE dormitory_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dormitory_id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                roomDTO = new RoomDTO();
                roomDTO.setRoomId(rs.getInt("room_id"));
                roomDTO.setDormitoryId(rs.getInt("dormitory_id"));
                roomDTO.setRoomNumber(rs.getString("room_number"));
                roomDTO.setRoomType(rs.getString("room_type"));
                roomDTO.setCapacity(rs.getInt("capacity"));
                roomDTO.setFee(rs.getInt("fee"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return roomDTO;
    }

    public boolean updateRoomFeeByDormitoryId(int dormitoryId, int fee){
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = ds.getConnection();
            // room_number,room_type, capacity, 일단 제외
            String sql = "UPDATE room SET fee = ? WHERE dormitory_id = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, fee);
            pstmt.setInt(2, dormitoryId);


            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    public boolean registerRoom(RoomDTO roomDTO) {  // boolean 대신 int 반환
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
            // room_number,room_type, capacity, 일단 제외
            String sql = "INSERT INTO room (dormitory_id,fee) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, roomDTO.getDormitoryId());
            pstmt.setInt(2, roomDTO.getFee());


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
