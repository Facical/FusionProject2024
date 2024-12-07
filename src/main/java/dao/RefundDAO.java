package dao;

import dto.RefundDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefundDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public List<RefundDTO> getRefundsByWithdrawId(int withdrawId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RefundDTO> refunds = new ArrayList<>();

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM refund WHERE withdraw_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, withdrawId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RefundDTO refund = new RefundDTO();
                refund.setRefundId(rs.getInt("refund_id"));
                refund.setWithdrawId(rs.getInt("withdraw_id"));
                refund.setAmount(rs.getInt("amount"));
                refund.setRefundDate(rs.getDate("refund_date"));
                refund.setProcessed(rs.getBoolean("is_processed"));
                refunds.add(refund);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return refunds;
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