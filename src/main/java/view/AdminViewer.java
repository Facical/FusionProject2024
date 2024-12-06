// AdminViewer.java
package view;

import java.util.Scanner;

public class AdminViewer {
    private static final Scanner scanner = new Scanner(System.in);

    public static int viewAdminPage() {
        while(true) {
            try {
                System.out.println("\n=== 관리자 메뉴 ===");
                System.out.println("[1] 선발 일정 등록");
                System.out.println("[2] 생활관 사용료 및 급식비 등록");
                System.out.println("[3] 신청자 조회");
                System.out.println("[4] 입사자 선발 및 호실 배정");
                System.out.println("[5] 생활관 비용 납부자 조회");
                System.out.println("[6] 생활관 비용 미납부자 조회");
                System.out.println("[7] 결핵진단서 제출 확인");
                System.out.println("[8] 퇴사 신청자 조회 및 환불");
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