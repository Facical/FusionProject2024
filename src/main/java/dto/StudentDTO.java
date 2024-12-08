package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentDTO {
    private int studentId;
    private int userId;
    private String name;
    private String contact;
    private String homeAddress;
    private double previous_grade;
    private String gender;          // 'M' 또는 'F'
    private String department;
    private String studentType;     // '학부생' or '대학원생'
}
