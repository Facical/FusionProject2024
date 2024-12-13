package service;

import java.util.HashMap;
import java.util.Map;

// 학생 정보를 바탕으로 해서 실제 기숙사 입사에 필요한 점수를 계산한다.
// 거리, 학점, 학생 타입(학부생 or 대학원생)을 활용한다.

public class ScoreCalculator {
    private static final Map<String, Double> DISTANCE_BONUS;

    static {
        DISTANCE_BONUS = new HashMap<>();
        DISTANCE_BONUS.put("서울", 0.5);
        DISTANCE_BONUS.put("대구", 0.1);
        DISTANCE_BONUS.put("부산", 0.3);
        DISTANCE_BONUS.put("인천", 0.4);
        DISTANCE_BONUS.put("광주", 0.2);
        DISTANCE_BONUS.put("대전", 0.3);
        DISTANCE_BONUS.put("울산", 0.2);
        DISTANCE_BONUS.put("제주", 0.7);
        DISTANCE_BONUS.put("기타", 0.1); // 기타 지역
    }

    public double calculateScore(double previousGrade, String homeAddress, String studentType) {
        double score = previousGrade; // 학점 반영

        // 거리 가산점 반영
        score += DISTANCE_BONUS.getOrDefault(homeAddress, DISTANCE_BONUS.get("기타"));

        // 학생 타입 가산점 반영
        if ("대학원생".equals(studentType)) {
            score += 100;
        }

        return score;
    }
}

