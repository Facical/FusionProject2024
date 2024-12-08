package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdmissionDTO {
    private int application_id;
    private int room_id;
    private String admission_status;
}
