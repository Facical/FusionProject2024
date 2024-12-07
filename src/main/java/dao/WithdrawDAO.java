package dao;

import dto.WithdrawDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WithdrawDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

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
                withdraw.setWithdrawId(rs.getInt("withdraw_id"));
                withdraw.setStudentId(rs.getInt("student_id"));
                withdraw.setWithdrawDate(rs.getDate("withdraw_date"));
                withdraw.setBankName(rs.getString("bank_name"));
                withdraw.setAccountNumber(rs.getString("account_number"));
                withdraws.add(withdraw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return withdraws;
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