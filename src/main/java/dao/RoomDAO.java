package dao;

import dto.MealDTO;
import dto.RoomDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
import java.util.*;

public class RoomDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

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

    public RoomDTO getRoomInfo(int room_id){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        RoomDTO roomDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM room WHERE room_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, room_id);
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

    public boolean registerRoom(RoomDTO roomDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ds.getConnection();
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

    // 새로운 메서드 1: 특정 기숙사의 모든 방 정보를 가져오기
    public List<RoomDTO> getRoomsByDormitoryId(int dormitoryId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RoomDTO> roomList = new ArrayList<>();

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM room WHERE dormitory_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dormitoryId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RoomDTO room = new RoomDTO();
                room.setRoomId(rs.getInt("room_id"));
                room.setDormitoryId(rs.getInt("dormitory_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setRoomType(rs.getString("room_type"));
                room.setCapacity(rs.getInt("capacity"));
                room.setFee(rs.getInt("fee"));
                roomList.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return roomList;
    }

    // 새로운 메서드 2: 모든 방 정보를 가져오기
    public List<RoomDTO> getAllRooms() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RoomDTO> roomList = new ArrayList<>();

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM room";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RoomDTO room = new RoomDTO();
                room.setRoomId(rs.getInt("room_id"));
                room.setDormitoryId(rs.getInt("dormitory_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setRoomType(rs.getString("room_type"));
                room.setCapacity(rs.getInt("capacity"));
                room.setFee(rs.getInt("fee"));
                roomList.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return roomList;
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
