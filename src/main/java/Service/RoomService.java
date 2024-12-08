package Service;

import dao.RoomDAO;
import dto.RoomDTO;

public class RoomService {
    private final RoomDAO roomDAO = new RoomDAO();


    public boolean registerRoom(RoomDTO roomDTO){
        return roomDAO.registerRoom(roomDTO);
    }
}
