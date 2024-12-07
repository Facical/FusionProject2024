package dao;

import dto.RefundDTO;

import javax.sql.DataSource;
import java.sql.*;

public class RefundDAO {
    private final DataSource ds = PooledDataSource.getDataSource();

    public RefundDTO findRefund(int refundId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        RefundDTO refund = null;

        try {
            conn = ds.getConnection();
            String sql = "SELECT * FROM refund WHERE refund_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, refundId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                refund = new RefundDTO();
                refund.setRefundId(rs.getInt("refund_id"));

                // refund_date 컬럼이 DATE 타입이라면 문자열로 변환
                // 날짜 형식을 포맷팅하고 싶다면 SimpleDateFormat 등을 사용할 수 있음
                // 여기서는 단순히 rs.getString()으로 받아옴
                // 만약 DATE 타입을 LocalDate, LocalDateTime으로 처리하려면 적절한 변환 로직 필요
                refund.setRefundDate(rs.getString("refund_date"));

                refund.setAmount(rs.getInt("amount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return refund;
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null)  rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
