package service;

import dao.RoomDAO;
import dto.RoomDTO;

//22222
public class RoomService {
    private final RoomDAO roomDAO = new RoomDAO();
    public RoomDTO getRoomInfo(int dormitory_id){
        return roomDAO.getRoomInfo(dormitory_id);
    }

    public boolean registerRoom(RoomDTO roomDTO){
        return roomDAO.registerRoom(roomDTO);
    }
    public boolean updateRoomFeeByDormitoryId(int dormitoryId, int fee){
        return roomDAO.updateRoomFeeByDormitoryId(dormitoryId, fee);
    }

}
