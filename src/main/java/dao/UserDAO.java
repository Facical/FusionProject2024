package dao;

import dto.UserDTO;

import javax.sql.DataSource;
import java.sql.*;

public class UserDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public UserDTO findUser(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        UserDTO user = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM user WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new UserDTO();
                user.setId(rs.getInt("user_id"));
                user.setPassword(rs.getInt("password"));
                user.setRole(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return user;
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