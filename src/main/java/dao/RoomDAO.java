package dao;

import dto.RoomDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class RoomDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public Map<Integer, List<RoomDTO>> findAllRoomsGroupedByDorm() {
        Map<Integer, List<RoomDTO>> map = new HashMap<>();
        String sql = "SELECT * FROM room";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                RoomDTO r = new RoomDTO();
                r.setRoomId(rs.getInt("room_id"));
                r.setDormitoryId(rs.getInt("dormitory_id"));
                r.setRoomNumber(rs.getString("room_number"));
                r.setRoomType(rs.getString("room_type"));
                r.setCapacity(rs.getInt("capacity"));
                r.setFee(rs.getInt("fee"));

                map.computeIfAbsent(r.getDormitoryId(), k -> new ArrayList<>()).add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
