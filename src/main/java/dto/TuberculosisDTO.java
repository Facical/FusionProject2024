// 결핵진단서 정보를 담는 DTO 클래스
package dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class TuberculosisDTO {
    private int certificationId;      // 진단서 고유 식별자
    private int studentId;            // 제출한 학생의 학번
    private Date submissionDate;      // 제출일
    private Date dueDate;             // 제출 기한
    private byte[] imageData;         // 제출된 진단서 이미지의 바이너리 데이터
    private String fileName;          // 원본 파일명
    private String fileType;          // 파일 확장자(jpg, png 등)
}