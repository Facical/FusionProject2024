package dao;

import dto.DormitoryDTO;
import dto.StudentDTO;
import dto.UserDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DormitoryDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    // dormitory_id를 받아오는 함수
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
