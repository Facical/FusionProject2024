package dao;

import dto.RoomDTO;
import javax.sql.DataSource;
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
                roomDTO.setRoom_id(rs.getInt("room_id"));
                roomDTO.setDormitory_id(rs.getInt("dormitory_id"));
                roomDTO.setRoom_number(rs.getInt("room_number"));
                roomDTO.setRoom_type(rs.getString("room_type"));
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
