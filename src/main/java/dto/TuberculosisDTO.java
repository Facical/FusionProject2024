package dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class TuberculosisDTO {
    private int certificationId;
    private int studentId;
    private Date submissionDate;
    private Date dueDate;
    private byte[] imageData;        // 이미지 파일 바이트 배열
    private String fileName;        // 파일명
    private String fileType;        // 파일 타입(확장자)
}