package student;

import java.util.Scanner;

public class StudentMenu {

    public void showMenu() {
        Scanner scanner = new Scanner(System.in);
        StudentService service = new StudentService();

       
        System.out.println("      Welcome to ExamGuard       ");
        

        //  input
        System.out.print("Enter your Student ID: ");
        String studentId = scanner.nextLine();

        System.out.print("Enter Exam ID: ");
        String examId = scanner.nextLine();

        System.out.println("\n1. Start Exam");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // clear buffer

        if (choice == 1) {
            service.startExam(studentId, examId);
        } else {
            System.out.println("Invalid choice. Please try again.");
        }

        scanner.close();
    }//,
}//