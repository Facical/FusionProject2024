package dao;

import dto.RefundDTO;
import dto.WithdrawDTO;

import javax.print.attribute.standard.RequestingUserName;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefundDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public boolean processRefunds(List<WithdrawDTO> withdraws) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = true;

        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);  //  ``@a3                                                     342  트랜잭션 시작

            String sql = "INSERT INTO refund (withdrawal_id, amount, refund_date, is_processed) VALUES (?, ?, ?, 2)";
            pstmt = conn.prepareStatement(sql);

            for (WithdrawDTO withdraw : withdraws) {
                pstmt.setInt(1, withdraw.getWithdrawalId());
                pstmt.setInt(2, withdraw.getRefundAmount());
                pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                //pstmt.setInt(4, withdraw.get);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();  // 트랜잭션 커밋

            // 모든 처리가 성공했는지 확인
            for (int result : results) {
                if (result <= 0) {
                    success = false;
                    break;
                }
            }
        } catch (SQLException e) {
            success = false;
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            closeResources(conn, pstmt, null);
        }
        return success;
    }

    public RefundDTO getRefundsByWithdrawId(int withdrawId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        RefundDTO refundDTO = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM refund WHERE withdrawal_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, withdrawId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                refundDTO = new RefundDTO();
                refundDTO.setRefundId(rs.getInt("refund_id"));
                refundDTO.setWithdrawalId(rs.getInt("withdrawal_id"));
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