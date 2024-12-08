package dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString

public class ApplicationDTO {
    private int applicationId;
    private int studentId;
    private String applicationDate;
    private int scheduleId;
}
