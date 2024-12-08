package dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentDTO {
    private int student_id;
    private int user_id;
    private String name;
    private String contact;
    private String home_address;
    private double previous_grade;
    private String gender;
    private String department;
    private String student_type;
}
