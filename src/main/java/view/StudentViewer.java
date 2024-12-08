package view;

import java.util.Scanner;

public class StudentViewer {
    private static final Scanner scanner = new Scanner(System.in);

    public static int viewStudentPage() {
        while(true) {
            try {
                System.out.println("\n=== 학생 메뉴 ===");
                System.out.println("[1] 선발 일정 및 비용 확인");
                System.out.println("[2] 입사 신청");
                System.out.println("[3] 합격 여부 및 호실 확인");
                System.out.println("[4] 생활관 비용 확인 및 납부");
                System.out.println("[5] 결핵진단서 제출");
                System.out.println("[6] 퇴사 신청");
                System.out.println("[7] 환불 확인");
                System.out.println("[0] 종료");
                System.out.print("메뉴 선택: ");

                int input = scanner.nextInt();
                if(input >= 0 && input <= 8) {
                    return input;
                }
                System.out.println("잘못된 메뉴 선택입니다.");
            } catch(Exception e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                scanner.nextLine(); // 버퍼 비우기
            }
        }
    }
}
