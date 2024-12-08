package dao;

import dto.AdmissionDTO;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdmissionDAO {
    private final DataSource ds = PooledDataSource.getDataSource();
    public boolean findCheckAdmission() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdmissionDTO admissionDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM admission WHERE application_id = ? AND room_id = ? AND admission_status = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, admissionDTO.getApplication_id());
            pstmt.setInt(2, admissionDTO.getRoom_id());
            pstmt.setString(3, admissionDTO.getAdmission_status());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                admissionDTO.setApplication_id(rs.getInt("application_id"));
                admissionDTO.setRoom_id(rs.getInt("room_id"));
                admissionDTO.setAdmission_status(rs.getString("admission_status"));
            }
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
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
