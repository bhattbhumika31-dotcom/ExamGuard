import java.util.Scanner;
import model.Exam;

public class StudentMenu {

    public void showMenu() {
        Scanner scanner = new Scanner(System.in);
        StudentService service = new StudentService();

        System.out.println("      Welcome to ExamGuard       ");

        System.out.print("Enter your Student ID: ");
        String studentId = scanner.nextLine();

        System.out.print("Enter your Name: ");
        String studentName = scanner.nextLine();

        System.out.println("\nAvailable Exams:");
        for (Exam exam : service.getAvailableExams()) {
            System.out.println("- " + exam.getExamId() + " | " + exam.getTitle()
                + " | " + exam.getSubject()
                + " | " + service.getExamStatus(studentId, exam.getExamId()));
        }

        System.out.print("Enter Exam ID: ");
        String examId = scanner.nextLine();

        System.out.println("\n1. Start Exam");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // clear buffer

        if (choice == 1) {
            service.startExam(studentId, studentName, examId);
        } else {
            System.out.println("Invalid choice. Please try again.");
        }

        scanner.close();
    }
}
