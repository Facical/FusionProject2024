package dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString

public class WithdrawDTO {
    private int withdrawalId;
    private int studentId;
    private String applicationDate;
    private String withdrawalDate;
    private String withdrawalType;
    private String bankName;
    private int accountNumber;
    private int refundAmount;
    private String withdrawalStatus;
    private String reason;
    private int dormitoryId;
}
