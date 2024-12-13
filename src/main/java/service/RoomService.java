package service;

import dao.RoomDAO;
import dto.RoomDTO;
// room서비스에 DAO에 관한 의존성을 주입하기 위함.
public class RoomService {
    private final RoomDAO roomDAO = new RoomDAO();

    public boolean updateRoomFeeByDormitoryId(int dormitoryId, int fee){
        return roomDAO.updateRoomFeeByDormitoryId(dormitoryId, fee);
    }
    public boolean registerRoom(RoomDTO roomDTO){
        return roomDAO.registerRoom(roomDTO);
    }
}
