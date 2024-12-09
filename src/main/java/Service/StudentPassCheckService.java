package service;

import dao.StudentPassDAO;
import dto.StudentPassDTO;

public class StudentPassCheckService {
    private final StudentPassDAO studentPassDAO;

    public StudentPassCheckService() {
        this.studentPassDAO = new StudentPassDAO();
    }

    /**
     * 특정 학생의 합격 정보를 조회합니다.
     *
     * @param studentId 학생 ID
     * @return 합격 정보를 포함한 StudentPassDTO 객체
     */
    public StudentPassDTO getStudentPassInfo(int studentId) {
        return studentPassDAO.findAdmissionAndRoomDetails(studentId);
    }

    /**
     * StudentPassDTO를 String으로 포맷합니다.
     *
     * @param studentPassDTO StudentPassDTO 객체
     * @return 포맷된 String
     */
    public String formatStudentPassInfo(StudentPassDTO studentPassDTO) {
        if (studentPassDTO == null) {
            return "합격 정보가 없습니다.";
        }
        return String.format("Dormitory: %s, Room: %d, Bed: %d",
                studentPassDTO.getDormitoryName(),
                studentPassDTO.getRoomNumber(),
                studentPassDTO.getBedNumber());
    }
}
