package view;



import java.util.Scanner;

public class StudentViewer {

    public static int viewStudentPage()
    {
        Scanner sc = new Scanner(System.in);

        System.out.println("기능을 선택하세요");
        System.out.println("[1] 선발 일정 및 비용 확인");
        int input = sc.nextInt();

        // 나머지 구현
        return input;
    }
}
