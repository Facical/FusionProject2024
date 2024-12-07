package dto;

import lombok.Getter;
import lombok.Setter;
import java.sql.Date;

@Getter
@Setter
public class WithdrawDTO {
    private int withdrawId;
    private int studentId;
    private Date withdrawDate;
    private String bankName;
    private String accountNumber;
}