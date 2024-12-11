package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawDetailDTO {
    private int withdrawalId;
    private int studentId;
    private String studentName;
    private String dormitoryName;
    private String withdrawalDate;
    private String bankName;
    private String accountNumber;
    private int refundAmount;
    private String withdrawalStatus;
}