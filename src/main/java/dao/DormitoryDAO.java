package dao;

import dto.DormitoryDTO;
import dto.UserDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DormitoryDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public DormitoryDTO findDormitoryId() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DormitoryDTO dormitoryDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM dormitory WHERE dormitory_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dormitoryDTO.getDormitoryId());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                dormitoryDTO.setDormitoryId(rs.getInt("dormitory_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return dormitoryDTO;
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
