package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDTO {
    private int roomId;
    private int dormitoryId;
    private String roomNumber;
    private String roomType; // 예: 2인실/4인실 enum
    private int capacity;
    private int fee;

    // 실제 DB에 assigned_count가 없으니, 실시간 업데이트를 위해 in-memory로 관리할 수도 있음.
    // 여기서는 capacity를 정원으로 보고, 배정할 때마다 capacity를 1씩 줄이는 방식 사용.
    // 만약 assignedCount 컬럼을 추가했다면 DAO에서 관리 필요.
    public boolean hasSpace() {
        return capacity > 0;
    }

    public int assignBed() {
        // capacity를 빈 자리 수로 보고 bed_number는 (정원 - 남은 자리 + 1)등으로 할당
        // 여기서는 단순히 capacity 줄이고, bed_number 대략 할당
        int assignedBedNumber = capacity;
        capacity = capacity - 1;
        return assignedBedNumber;
    }
}
