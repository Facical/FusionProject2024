package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundDTO {
    private int refundId;
    private String refundDate;
    private int amount;
}
