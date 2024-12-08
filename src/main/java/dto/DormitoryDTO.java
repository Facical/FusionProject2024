package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DormitoryDTO {
    private int dormitoryId;
    private String name;
    // 성별 제한: 'M', 'F', 'ALL' 등을 가정할 수 있으나
    // 여기서는 스키마에 없는 필드이므로 필요하다면 추가 가정
    // 없으면 모든 성별 수용한다고 가정하거나, room_type 기반으로 성별 필터.
    // 본 스키마엔 allowed_gender 직접 안보이므로, room_type 등을 활용해야 한다.
    // room_type에 '2인실/4인실'만 보이므로 여기선 성별 제한 없는 것으로 가정.
    // 필요하다면 dormitory 테이블에 allowed_gender 필드 추가.
}
