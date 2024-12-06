package Service;

import dao.RoomDAO;
import dto.RoomDTO;

public class RoomService {
    private final RoomDAO roomDAO;

    public RoomService(RoomDAO roomDAO){
        this.roomDAO = roomDAO;
    }
    public boolean registerRoom(RoomDTO roomDTO){
        return roomDAO.registerRoom(roomDTO);
    }
}
