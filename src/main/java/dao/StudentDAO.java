package dao;

import dto.ScheduleDTO;
import dto.StudentDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public List<StudentDTO> getAllStudentInfo() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentDTO> students = new ArrayList<>();
        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM student";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                StudentDTO studentDTO = new StudentDTO();
                studentDTO.setStudent_id(rs.getInt("student_id"));
                studentDTO.setUser_id(rs.getInt("user_id"));
                studentDTO.setName(rs.getTimestamp("name").toString());
                studentDTO.setContact(rs.getTimestamp("contact").toString());
                studentDTO.setHome_address(rs.getTimestamp("home_address").toString());
                studentDTO.setPrevious_grade(rs.getDouble("previous_grade"));
                studentDTO.setGender(rs.getTimestamp("gender").toString());
                studentDTO.setDepartment(rs.getTimestamp("department").toString());
                studentDTO.setStudent_type(rs.getTimestamp("student_type").toString());

                students.add(studentDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return students;
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
