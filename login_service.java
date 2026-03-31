package auth;

import java.util.Scanner;

public class LoginService {

    private Scanner scanner;

    public LoginService() {
        scanner = new Scanner(System.in);
    }

    public String login() {
        System.out.println("===== ExamGuard Login =====");
        System.out.println("1. Login as Teacher");
        System.out.println("2. Login as Student");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                return "TEACHER";
            case 2:
                return "STUDENT";
            default:
                System.out.println("Invalid choice! Try again.");
                return login(); // retry
        }
    }
}