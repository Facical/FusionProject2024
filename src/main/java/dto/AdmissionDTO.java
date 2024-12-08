package dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdmissionDTO {
    private int admissionId;
    private int applicationId;
    private int studentId;
    private int roomId;
    private int bedNumber;
    private int dormitoryId;
    private LocalDate admissionDate;
    private LocalDate residenceStartDate;
    private LocalDate residenceEndDate;
    private String admissionStatus;     // '입사', '입사 취소'
    private String certificateStatus;   // '제출 완료', '미제출'
    private String paymentStatus;       // '납부 완료', '미납부'
}
