package dao;

import dto.RefundDTO;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefundDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public RefundDTO getRefundsByWithdrawId(int withdrawId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        RefundDTO refundDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM refund WHERE withdraw_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, withdrawId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                refundDTO = new RefundDTO();
                refundDTO.setRefundId(rs.getInt("refund_id"));
                refundDTO.setWithdrawId(rs.getInt("withdraw_id"));
                refundDTO.setAmount(rs.getInt("amount"));
                refundDTO.setRefundDate(rs.getDate("refund_date"));
                refundDTO.setIsProcessed(rs.getInt("is_processed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return refundDTO;
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