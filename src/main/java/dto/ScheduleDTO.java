// ScheduleDTO.java
package dto;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class ScheduleDTO {
    private int scheduleId;
    private String periodName;
    private String startDate;  // YYYY-MM-DD
    private String startHour;  // HH:mm:ss
    private String endDate;    // YYYY-MM-DD
    private String endHour;    // HH:mm:ss

}