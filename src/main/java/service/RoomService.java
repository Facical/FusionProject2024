package service;

import dao.RoomDAO;
import dto.RoomDTO;
//ddddㅇㅇ
public class RoomService {
    private final RoomDAO roomDAO = new RoomDAO();
    public RoomDTO getRoomInfo(int dormitory_id){
        return roomDAO.getRoomInfo(dormitory_id);
    }

    public boolean registerRoom(RoomDTO roomDTO){
        return roomDAO.registerRoom(roomDTO);
    }

}
