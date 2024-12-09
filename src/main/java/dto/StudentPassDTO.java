package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentPassDTO {
    private String studentId;
    private String dormitoryName; // 생활관 명
    private int roomNumber; // 호실
    private int bedNumber; // 침대 번호
}