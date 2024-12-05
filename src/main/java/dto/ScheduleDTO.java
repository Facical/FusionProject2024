package dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
public class ScheduleDTO {

    private int scheduleId;
    private String periodName;
    private String startDate;
    private String endDate;
}
