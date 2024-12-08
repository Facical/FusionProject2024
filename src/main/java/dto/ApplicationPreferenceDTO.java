package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationPreferenceDTO {
    private int applicationPreferenceID;
    private int applicationID;
    private int preferenceOrder;
    private int dormitoryID;
    private int mealID; // 식사 유형 등 추가 필드(예: 1: 조식만, 2: 1일 3식 등)

    // 필요시 생성자, toString, equals, hashCode 메서드 추가
    // Lombok 어노테이션(@AllArgsConstructor, @NoArgsConstructor, @ToString) 등을 사용할 수도 있음.
}
