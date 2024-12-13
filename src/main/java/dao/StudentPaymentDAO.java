package dao;

import dto.StudentPaymentDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 학생의 납부 정보를 조회하는 클래스
public class StudentPaymentDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    // 납부한 학생들의 정보를 가져오는 함수
    public List<StudentPaymentDTO> getPaidStudentList() {
        List<StudentPaymentDTO> paidStudents = new ArrayList<>();
        String sql = "SELECT s.name AS student_name, d.name AS dormitory_name " +
                "FROM admission a " +
                "JOIN student s ON a.student_id = s.student_id " +
                "JOIN room r ON a.room_id = r.room_id " +
                "JOIN dormitory d ON r.dormitory_id = d.dormitory_id " +
                "WHERE a.payment_status = '납부 완료' " +
                "ORDER BY d.name";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StudentPaymentDTO studentPayment = new StudentPaymentDTO();
                studentPayment.setStudentName(rs.getString("student_name"));
                studentPayment.setDormitoryName(rs.getString("dormitory_name"));
                paidStudents.add(studentPayment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paidStudents;
    }

    // 미납부한 학생들의 정보를 가져오는 함수
    public List<StudentPaymentDTO> getUnpaidStudentList() {
        List<StudentPaymentDTO> unpaidStudents = new ArrayList<>();
        String sql = "SELECT s.name AS student_name, d.name AS dormitory_name " +
                "FROM admission a " +
                "JOIN student s ON a.student_id = s.student_id " +
                "JOIN room r ON a.room_id = r.room_id " +
                "JOIN dormitory d ON r.dormitory_id = d.dormitory_id " +
                "WHERE a.payment_status = '미납' " +
                "ORDER BY d.name";

        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StudentPaymentDTO studentPayment = new StudentPaymentDTO();
                studentPayment.setStudentName(rs.getString("student_name"));
                studentPayment.setDormitoryName(rs.getString("dormitory_name"));
                unpaidStudents.add(studentPayment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return unpaidStudents;
    }
}
