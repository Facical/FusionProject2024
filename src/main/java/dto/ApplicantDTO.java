package dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicantDTO {
    private int studentId;
    private String name;
    private String gender;
    private String homeAddress;
    private double previousGrade;
    private String studentType;

    private int applicationId;

    private int firstDormitoryId;
    private String firstDormitoryName;
    private int firstMealId;
    private String firstMealName;

    private int secondDormitoryId;
    private String secondDormitoryName;
    private int secondMealId;
    private String secondMealName;

    private double score;

    private int roomId;
    private String roomNumber; // 방 번호
    private int bedNumber; // 침대 번호
    private int dormitoryId;
}

