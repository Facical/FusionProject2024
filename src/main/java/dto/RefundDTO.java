package dto;

import lombok.Getter;
import lombok.Setter;
import java.sql.Date;

@Getter
@Setter
public class RefundDTO {
    private int refundId;       // 환불 고유 ID
    private int withdrawalId;   // 연관된 출금 요청 ID
    private int amount;         // 환불 금액
    private Date refundDate;    // 환불 처리 날짜
    private int isProcessed;    // 환불 처리 여부 (0: 미처리, 1: 처리 완료)
}
