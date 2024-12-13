package service;

import dao.RoomDAO;
import dto.RoomDTO;

public class RoomService {
    private final RoomDAO roomDAO = new RoomDAO();

    public boolean updateRoomFeeByDormitoryId(int dormitoryId, int fee){
        return roomDAO.updateRoomFeeByDormitoryId(dormitoryId, fee);
    }
}
