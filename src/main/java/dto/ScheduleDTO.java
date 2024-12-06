// ScheduleDTO.java
package dto;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class ScheduleDTO {
    private int scheduleId;
    private String periodName;    // 기간 이름 (ex: "생활관 입사 신청", "결핵진단서 제출")
    private String startDate;  // 시작일
    private String endDate;    // 종료일
}