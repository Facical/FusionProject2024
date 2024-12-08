package dao;

import dto.WithdrawDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WithdrawDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

    public List<WithdrawDTO> getAllWithdraws() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<WithdrawDTO> withdraws = new ArrayList<>();

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM withdrawal_application ORDER BY withdraw_date";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                WithdrawDTO withdraw = new WithdrawDTO();
                withdraw.setWithdrawalId(rs.getInt("withdraw_id"));
                withdraw.setStudentId(rs.getInt("student_id"));
                withdraw.setWithdrawalDate(rs.getString("withdraw_date"));
                withdraw.setBankName(rs.getString("bank_name"));
                withdraw.setAccountNumber(rs.getInt("account_number"));
                withdraws.add(withdraw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return withdraws;
    }
    public WithdrawDTO getWithdrawInfo(int studentID){
        /*
        학생의 학번을 기준으로 환불 정보를 가져오는 함수
         */
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        WithdrawDTO withdrawDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM withdrawal_application WHERE student_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                withdrawDTO = new WithdrawDTO();
                withdrawDTO.setWithdrawalId(rs.getInt("withdrawal_id"));
                withdrawDTO.setStudentId(rs.getInt("student_id"));
                withdrawDTO.setApplicationDate(rs.getString("application_date"));
                withdrawDTO.setWithdrawalDate(rs.getString("withdrawal_date"));
                withdrawDTO.setWithdrawalType(rs.getString("withdrawal_type"));
                withdrawDTO.setBankName(rs.getString("bank_name"));
                withdrawDTO.setAccountNumber(rs.getInt("account_number"));
                withdrawDTO.setRefundAmount(rs.getInt("refund_amount"));
                withdrawDTO.setWithdrawalStatus(rs.getString("withdrawal_status"));
                withdrawDTO.setReason(rs.getString("reason"));
                withdrawDTO.setDormitoryId(rs.getInt("dormitory_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return withdrawDTO;

    }


    public void setWithdrawInfo(WithdrawDTO withdrawDTO){
        Connection conn = null;
        PreparedStatement pstmt = null;
        int updateResult;
        /*
        withdrawal_id 를 지정해서 넣는 것이 맞는 건가?
         */
        try {
            conn = ds.getConnection();
            String sql = "INSERT INTO withdrawal_application " +
                    "(student_id, application_date, withdrawal_type, bank_name, " +
                    "account_number, refund_amount, withdrawal_status, reason, dormitory_id)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            //pstmt.setInt(1, withdrawDTO.getWithdrawalId());
            pstmt.setInt(1, withdrawDTO.getStudentId());
            pstmt.setString(2, withdrawDTO.getApplicationDate());
            //pstmt.setString(4, withdrawDTO.getWithdrawalDate());
            pstmt.setString(3, withdrawDTO.getWithdrawalType());
            pstmt.setString(4, withdrawDTO.getBankName());
            pstmt.setInt(5, withdrawDTO.getAccountNumber());
            pstmt.setInt(6, withdrawDTO.getRefundAmount());
            pstmt.setString(7, withdrawDTO.getWithdrawalStatus());
            pstmt.setString(8, withdrawDTO.getReason());
            pstmt.setInt(9, withdrawDTO.getDormitoryId());

            updateResult = pstmt.executeUpdate();

            if (updateResult > 0) {
                System.out.println("Update successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt);
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

    private void closeResources(Connection conn, PreparedStatement pstmt) {
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}