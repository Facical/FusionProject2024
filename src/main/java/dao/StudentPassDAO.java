package dao;

import javax.sql.DataSource;
import dto.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentPassDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public StudentPassDTO findAdmissionAndRoomDetails(int studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StudentPassDTO studentPassDTO = null;

        try {
            conn = ds.getConnection();
            // Join을 통해 필요한 정보를 가져오는 쿼리
            String sql = "SELECT s.student_id AS student_id, d.name AS dormitory_name, " +
                    "       r.room_number AS room_number, a.bed_number AS bed_number " +
                    "FROM admission a " +
                    "JOIN student s ON a.student_id = s.student_id " +
                    "JOIN room r ON a.room_id = r.room_id " +
                    "JOIN dormitory d ON r.dormitory_id = d.dormitory_id " +
                    "WHERE s.student_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                studentPassDTO = new StudentPassDTO();
                studentPassDTO.setStudentId(rs.getString("student_id"));
                studentPassDTO.setDormitoryName(rs.getString("dormitory_name"));
                studentPassDTO.setRoomNumber(rs.getInt("room_number"));
                studentPassDTO.setBedNumber(rs.getInt("bed_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return studentPassDTO;
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
