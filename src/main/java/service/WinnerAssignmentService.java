package service;

import dao.AdmissionDAO;
import dao.RoomDAO;
import dto.AdmissionDTO;
import dto.ApplicantDTO;
import dto.RoomDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class WinnerAssignmentService {

    // 기숙사와 방 상태를 관리하는 맵
    private final Map<String, List<RoomDTO>> dormitoryRooms = new HashMap<>();

    public WinnerAssignmentService() {
        // 초기화 시 DB에서 방 정보 로드
        List<RoomDTO> rooms = loadRoomsFromDB();
        for (RoomDTO room : rooms) {
            String dormitoryName = getDormitoryNameById(room.getDormitoryId());
            dormitoryRooms.putIfAbsent(dormitoryName, new ArrayList<>());
            dormitoryRooms.get(dormitoryName).add(room);
        }
    }


    private List<RoomDTO> loadRoomsFromDB() {
        // RoomDAO를 통해 DB에서 방 정보 로드
        RoomDAO roomDAO = new RoomDAO();
        return roomDAO.getAllRooms();
    }

    private String getDormitoryNameById(int dormitoryId) {
        Map<Integer, String> dormitoryNames = Map.of(
                1, "푸름관1동", 2, "푸름관2동", 3, "푸름관3동",
                4, "푸름관4동", 5, "오름관1동", 6, "오름관2동", 7, "오름관3동"
        );
        return dormitoryNames.getOrDefault(dormitoryId, "Unknown Dormitory");
    }

    public List<Map<String, List<ApplicantDTO>>> assignApplicantsToDormitories(List<ApplicantDTO> applicants) {

        // 성별별 기숙사 초기화
        Map<String, List<ApplicantDTO>> maleDormitories = new HashMap<>();
        Map<String, List<ApplicantDTO>> femaleDormitories = new HashMap<>();

        for (String dorm : dormitoryRooms.keySet()) {
            maleDormitories.put(dorm, new ArrayList<>());
            femaleDormitories.put(dorm, new ArrayList<>());
        }

        // 성별 리스트 분리 및 정렬
        List<ApplicantDTO> maleApplicants = applicants.stream()
                .filter(a -> a.getGender().equals("M"))
                .sorted(Comparator.comparingDouble(ApplicantDTO::getScore).reversed())
                .collect(Collectors.toList());

        List<ApplicantDTO> femaleApplicants = applicants.stream()
                .filter(a -> a.getGender().equals("F"))
                .sorted(Comparator.comparingDouble(ApplicantDTO::getScore).reversed())
                .collect(Collectors.toList());

        // 남학생 기숙사 배정
        assignToDormitories(maleApplicants, maleDormitories);
        assignRoomsAndBeds(maleDormitories);

        // 여학생 기숙사 배정
        assignToDormitories(femaleApplicants, femaleDormitories);
        assignRoomsAndBeds(femaleDormitories);

        // 결과 반환
        List<Map<String, List<ApplicantDTO>>> result = new ArrayList<>();
        result.add(maleDormitories);
        result.add(femaleDormitories);

        return result;
    }


    private void assignToDormitories(List<ApplicantDTO> applicants, Map<String, List<ApplicantDTO>> dormitories) {
        for (ApplicantDTO applicant : applicants) {
            String firstChoice = applicant.getFirstDormitoryName();
            String secondChoice = applicant.getSecondDormitoryName();

            System.out.println("지원자 배정 시도: " + applicant);
            System.out.println("1지망: " + firstChoice + ", 2지망: " + secondChoice);

            boolean assigned = assignApplicantToDormitory(applicant, firstChoice, dormitories);
            if (!assigned) {
                assigned = assignApplicantToDormitory(applicant, secondChoice, dormitories);
            }

            if (!assigned) {
                System.out.println("지원자 배정 실패: " + applicant);
            }
        }
    }

    private boolean assignApplicantToDormitory(ApplicantDTO applicant, String dormitoryName, Map<String, List<ApplicantDTO>> dormitories) {
        List<RoomDTO> rooms = dormitoryRooms.get(dormitoryName);

        if (rooms == null) {
            System.out.println("기숙사 방 정보가 없습니다: " + dormitoryName);
            return false;
        }

        if (isDormitoryFull(rooms)) {
            System.out.println("기숙사 정원이 가득 찼습니다: " + dormitoryName);
            return false;
        }

        // 지원자 배정
        dormitories.get(dormitoryName).add(applicant);

        // 배정된 기숙사 ID 저장
        int dormitoryId = getDormitoryIdByName(dormitoryName);
        applicant.setDormitoryId(dormitoryId);

        System.out.println("기숙사 배정 완료: " + applicant + " -> " + dormitoryName + " (ID: " + dormitoryId + ")");
        return true;
    }

    private int getDormitoryIdByName(String dormitoryName) {
        Map<String, Integer> dormitoryIds = Map.of(
                "푸름관1동", 1, "푸름관2동", 2, "푸름관3동", 3,
                "푸름관4동", 4, "오름관1동", 5, "오름관2동", 6, "오름관3동", 7
        );
        return dormitoryIds.getOrDefault(dormitoryName, -1); // -1은 오류를 나타냄
    }


    private boolean isDormitoryFull(List<RoomDTO> rooms) {
        for (RoomDTO room : rooms) {
            if (room.getCapacity() > 0) {
                System.out.println("방에 여유가 있습니다: " + room);
                return false; // 빈 자리가 있음
            }
        }
        System.out.println("모든 방이 가득 찼습니다.");
        return true; // 모든 방이 가득 참
    }

    private void assignRoomsAndBeds(Map<String, List<ApplicantDTO>> dormitories) {
        for (String dormitoryName : dormitories.keySet()) {
            List<ApplicantDTO> applicants = dormitories.get(dormitoryName);
            List<RoomDTO> rooms = dormitoryRooms.get(dormitoryName); // 방 정보 가져오기

            if (rooms == null) continue;

            for (ApplicantDTO applicant : applicants) {
                for (RoomDTO room : rooms) {
                    if (room.getCapacity() > 0) { // 빈 침대가 있는지 확인
                        int bedNumber = room.getCapacity(); // 현재 방의 남은 침대 수
                        applicant.setRoomId(room.getRoomId()); // 방 ID 설정
                        applicant.setRoomNumber(room.getRoomNumber()); // 방 번호 설정
                        applicant.setBedNumber(bedNumber); // 침대 번호 설정

                        // 방의 남은 침대 수 업데이트
                        room.setCapacity(room.getCapacity() - 1);
                        break;
                    }
                }
            }
        }
    }

    public void saveDormitoryAssignments(Map<String, List<ApplicantDTO>> dormitories) {
        AdmissionDAO admissionDAO = new AdmissionDAO();
        for (Map.Entry<String, List<ApplicantDTO>> entry : dormitories.entrySet()) {
            for (ApplicantDTO applicant : entry.getValue()) {
                AdmissionDTO admission = new AdmissionDTO();
                admission.setApplicationId(applicant.getApplicationId());
                admission.setDormitoryId(applicant.getDormitoryId());
                admission.setRoomId(applicant.getRoomId());
                admission.setBedNumber(applicant.getBedNumber());
                admission.setAdmissionDate(LocalDate.now());
                admission.setResidenceStartDate(LocalDate.of(2024, 3, 1));
                admission.setResidenceEndDate(LocalDate.of(2025, 2, 28));
                admission.setAdmissionStatus("입사");
                admission.setCertificateStatus("미완료");
                admission.setPaymentStatus("미납");
                admission.setStudentId(applicant.getStudentId());

                admissionDAO.saveAdmission(admission);
            }
        }
    }
}
