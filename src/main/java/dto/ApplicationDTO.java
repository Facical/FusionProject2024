package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ApplicationDTO {
    private int application_id;
    private int student_id;
    private String application_date;
    private int schedule_id;

}
