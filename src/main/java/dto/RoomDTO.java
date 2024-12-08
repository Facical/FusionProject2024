package dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDTO {
    private int roomId;
    private int dormitoryId;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private int fee;

}
