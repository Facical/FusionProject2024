package Service;

import dao.*;
import dto.*;

import java.util.*;

public class AllocationService {
    private StudentDAO studentDAO;
    private ApplicationDAO applicationDAO;
    private DormitoryDAO dormitoryDAO;
    private RoomDAO roomDAO;
    private AdmissionDAO admissionDAO;

    public AllocationService() {
        studentDAO = new StudentDAO();
        applicationDAO = new ApplicationDAO();
        dormitoryDAO = new DormitoryDAO();
        roomDAO = new RoomDAO();
        admissionDAO = new AdmissionDAO();
    }

    public void allocate() {
        // 1) 데이터 로딩
        Map<Integer, StudentDTO> studentMap = studentDAO.findAllStudentsAsMap();
        List<ApplicationDTO> applications = applicationDAO.findAllApplications();
        Map<Integer, DormitoryDTO> dormMap = dormitoryDAO.findAllDormitoriesAsMap();
        Map<Integer, List<RoomDTO>> roomMapByDorm = roomDAO.findAllRoomsGroupedByDorm();

        // 2) 점수 계산 및 정렬
        // 대학원생 우선 선발: 대학원생이면 기본 점수를 크게 부여
        // 학부생은 previous_grade(학점)와 주소 가점
        // 예: gpa = previous_grade
        //     지역 가점: 서울/경기 = +0.3, 대구 = +0.1 등
        //     대학원생: base score +100
        for (ApplicationDTO app : applications) {
            StudentDTO st = studentMap.get(app.getStudentId());
            double score = 0.0;

            if ("대학원생".equals(st.getStudentType())) {
                score += 100;
            }

            // 학점 가산
            score += st.getPrevious_grade();

            // 주소 가점 (예시)
            String addr = st.getHomeAddress();
            if (addr.contains("서울") || addr.contains("경기")) {
                score += 0.3;
            } else if (addr.contains("대구")) {
                score += 0.1;
            }

            // 성별에 따른 별도 로직이 필요하면 추가
            // 여기서는 점수 차별을 두지 않고 동일하게 처리

            app.setScore(score);
        }

        // 점수별 정렬(내림차순)
        applications.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 3) 관 별로 리스트에 배정
        // admission 기록을 위한 리스트
        List<AdmissionDTO> admissionList = new ArrayList<>();

        for (ApplicationDTO app : applications) {
            StudentDTO st = studentMap.get(app.getStudentId());
            boolean assigned = false;

            if (app.getPreferences() == null || app.getPreferences().isEmpty()) {
                continue;
            }

            for (ApplicationPreferenceDTO pref : app.getPreferences()) {
                int dormId = pref.getDormitoryID();
                List<RoomDTO> rooms = roomMapByDorm.get(dormId);
                if (rooms == null) continue;

                // 방 중 하나에 자리 있으면 배정
                RoomDTO assignedRoom = findAvailableRoom(rooms);
                if (assignedRoom != null) {
                    // 배정
                    AdmissionDTO ad = new AdmissionDTO();
                    ad.setApplicationId(app.getApplicationId());
                    ad.setStudentId(app.getStudentId());
                    ad.setRoomId(assignedRoom.getRoomId());
                    ad.setBedNumber(assignedRoom.assignBed());
                    ad.setAdmissionStatus("입사");
                    ad.setCertificateStatus("미제출");
                    ad.setPaymentStatus("미납부");
                    admissionList.add(ad);
                    assigned = true;
                    break;
                }
            }
            // assigned=false면 배정 실패 → 별도 처리 없음(정원 미달 시 어쩔 수 없음)
        }

        // 4) Admission에 합격자 삽입
        for (AdmissionDTO ad : admissionList) {
            admissionDAO.insertAdmission(ad);
        }

        System.out.println("입사자 선발 및 배정 완료");
    }

    private RoomDTO findAvailableRoom(List<RoomDTO> rooms) {
        for (RoomDTO r : rooms) {
            if (r.hasSpace()) {
                return r;
            }
        }
        return null;
    }
}
