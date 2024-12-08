package dao;

import dto.StudentDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StudentDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public Map<Integer, StudentDTO> findAllStudentsAsMap() {
        Map<Integer, StudentDTO> map = new HashMap<>();
        String sql = "SELECT * FROM student";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                StudentDTO s = new StudentDTO();
                s.setStudentId(rs.getInt("student_id"));
                s.setUserId(rs.getInt("user_id"));
                s.setName(rs.getString("name"));
                s.setContact(rs.getString("contact"));
                s.setHomeAddress(rs.getString("home_address"));
                s.setPrevious_grade(rs.getDouble("previous_grade"));
                s.setGender(rs.getString("gender"));
                s.setDepartment(rs.getString("department"));
                s.setStudentType(rs.getString("student_type"));
                map.put(s.getStudentId(), s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
