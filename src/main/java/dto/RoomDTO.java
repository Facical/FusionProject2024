package dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString

public class RoomDTO {
    private int room_id;
    private int dormitory_id;
    private int room_number;
    private String room_type;
    private int capacity;
    private int fee;
}
