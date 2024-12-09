package dto;

import lombok.Getter;
import lombok.Setter;
import java.sql.Date;

@Getter
@Setter
public class RefundDTO {
    private int refundId;
    private int withdrawId;
    private int amount;
    private Date refundDate;
    private int isProcessed;
}
