package dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ApplicantInfoDTO {
    private String studentName;
    private LocalDate applicationDate;
    private String dormitoryName; // 생활관 이름 필드 추가
}
