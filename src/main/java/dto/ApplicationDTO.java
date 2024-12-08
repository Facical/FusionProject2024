package dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ApplicationDTO {
    private int applicationId;
    private int studentId;
    private LocalDate applicationDate;
    private int scheduleId;

    // 선호도 리스트 추가
    private List<ApplicationPreferenceDTO> preferences;

    // 점수 필드 추가
    private double score;
}
