package dto;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class SelectionScheduleDTO {
    private int scheduleId;      // schedule_id  db에 자동으로 1씩 오르게하는 방법을 몰라서 찾아보기
    private String periodName;   // period_name (ex: "생활관 입사 신청", "결핵진단서 제출" 등)
    private Timestamp startDate;      // start_date
    private Timestamp endDate;       // end_date

}