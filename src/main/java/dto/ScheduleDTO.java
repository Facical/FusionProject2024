// ScheduleDTO.java
package dto;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class ScheduleDTO {
    private int scheduleId;
    private String periodName; // 기간명
    private String startDate;  // 시작 일자 YYYY-MM-DD
    private String startHour;  // 시작 시간 HH:mm:ss
    private String endDate;    // 종료 일자 YYYY-MM-DD
    private String endHour;    // 종료 시간 HH:mm:ss

}