package view;



import java.util.Scanner;

public class StudentViewer {

    public static int viewStudentPage()
    {
        Scanner sc = new Scanner(System.in);

        System.out.println("기능을 선택하세요");
        System.out.println("[1] 선발 일정 및 비용 확인");
        System.out.println("[4] 생활관 비용 확인 및 납부");
        System.out.println("[6] 퇴사 신청");
        System.out.println("[7] 환불 확인");
        System.out.println("[8] 프로그램 종료");
        int input = sc.nextInt();

        // 나머지 구현
        return input;
    }
}
