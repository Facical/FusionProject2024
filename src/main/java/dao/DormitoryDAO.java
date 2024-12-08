package dao;

import dto.DormitoryDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DormitoryDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public DormitoryDTO findDormitory(int dormitoryId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DormitoryDTO dormitory = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM dormitory WHERE dormitory_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dormitoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                dormitory = new DormitoryDTO();
                dormitory.setDormitoryId(rs.getInt("dormitory_id"));
                dormitory.setName(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return dormitory;
    }

    public Map<Integer, DormitoryDTO> findAllDormitoriesAsMap() {
        Map<Integer, DormitoryDTO> map = new HashMap<>();
        String sql = "SELECT * FROM dormitory";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                DormitoryDTO d = new DormitoryDTO();
                d.setDormitoryId(rs.getInt("dormitory_id"));
                d.setName(rs.getString("name"));
                map.put(d.getDormitoryId(), d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null)  rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
