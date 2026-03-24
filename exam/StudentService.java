import core.ExamGuardRepository;
import model.Exam;
import model.Question;
import model.Result;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class StudentService {

    private static final DateTimeFormatter ATTEMPT_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ExamGuardRepository repository = ExamGuardRepository.getInstance();

    public Result startExam(String studentId, String examId) {
        return startExam(studentId, studentId, examId);
    }

    public Result startExam(String studentId, String studentName, String examId) {
        Exam exam = validateExam(examId);
        if (exam == null) {
            return null;
        }

        long startedAt = System.currentTimeMillis();
        int score = 0;
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nExam started successfully: " + exam.getTitle());
        System.out.println("Subject: " + exam.getSubject());
        System.out.println("Duration: " + exam.getDurationMinutes() + " minutes");

        for (Question question : exam.getQuestions()) {
            displayQuestion(question);
            System.out.print("Your answer (A/B/C/D): ");
            String answer = scanner.nextLine().trim().toUpperCase();
            if (!answer.isEmpty() && question.isCorrect(answer.charAt(0))) {
                score += question.getMarks();
            }
        }

        Result result = buildResult(studentId, studentName, exam, score, startedAt);
        repository.addOrUpdateResult(result);
        printSubmissionSummary(result);
        return result;
    }

    public Result startExamWithDialog(Component parent, String studentId, String studentName, String examId) {
        Exam exam = validateExam(examId);
        if (exam == null) {
            JOptionPane.showMessageDialog(parent, "Exam not found or inactive.");
            return null;
        }
        if (exam.getQuestions().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "This exam has no questions yet.");
            return null;
        }

        long startedAt = System.currentTimeMillis();
        int score = 0;

        for (Question question : exam.getQuestions()) {
            String prompt = buildQuestionPrompt(question);
            String answer = JOptionPane.showInputDialog(parent, prompt, exam.getTitle(), JOptionPane.QUESTION_MESSAGE);
            if (answer == null) {
                int confirm = JOptionPane.showConfirmDialog(
                    parent,
                    "Cancel this attempt?",
                    "Exam Attempt",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    return null;
                }
                answer = "";
            }

            String normalized = answer.trim().toUpperCase();
            if (!normalized.isEmpty() && question.isCorrect(normalized.charAt(0))) {
                score += question.getMarks();
            }
        }

        Result result = buildResult(studentId, studentName, exam, score, startedAt);
        repository.addOrUpdateResult(result);
        JOptionPane.showMessageDialog(
            parent,
            "Exam submitted.\nScore: " + result.getScore() + "/" + result.getTotalMarks()
                + "\nPercentage: " + String.format("%.1f%%", result.getPercentage())
                + "\nGrade: " + result.getGrade(),
            "Submission Complete",
            JOptionPane.INFORMATION_MESSAGE);
        return result;
    }

    public List<Exam> getAvailableExams() {
        return repository.getActiveExams();
    }

    public List<Result> getStudentResults(String studentId) {
        return repository.getResultsForStudent(studentId);
    }

    private Exam validateExam(String examId) {
        Exam exam = repository.findExam(examId);
        if (exam == null || !exam.isActive()) {
            System.out.println("Sorry, exam not found or inactive.");
            return null;
        }
        if (exam.getQuestions().isEmpty()) {
            System.out.println("This exam has no questions yet.");
            return null;
        }
        return exam;
    }

    private Result buildResult(String studentId, String studentName, Exam exam, int score, long startedAt) {
        long elapsedSeconds = Math.max(1, (System.currentTimeMillis() - startedAt) / 1000);
        String resultId = "RES-" + exam.getExamId() + "-" + studentId;

        return new Result(
            resultId,
            studentId,
            studentName,
            exam.getExamId(),
            exam.getTitle(),
            score,
            exam.getTotalMarks(),
            elapsedSeconds,
            LocalDateTime.now().format(ATTEMPT_FORMAT));
    }

    private void displayQuestion(Question question) {
        System.out.println();
        System.out.println("Q" + question.getQuestionId() + ": " + question.getQuestionText());
        List<String> options = question.getAllOptions();
        char option = 'A';
        for (String value : options) {
            System.out.println(option + ") " + value);
            option++;
        }
    }

    private String buildQuestionPrompt(Question question) {
        StringBuilder builder = new StringBuilder();
        builder.append("Q").append(question.getQuestionId()).append(": ")
            .append(question.getQuestionText()).append("\n\n");

        List<String> options = question.getAllOptions();
        char option = 'A';
        for (String value : options) {
            builder.append(option).append(") ").append(value).append('\n');
            option++;
        }
        builder.append("\nEnter A, B, C, or D:");
        return builder.toString();
    }

    private void printSubmissionSummary(Result result) {
        System.out.println("\n===============================");
        System.out.println("Exam Submitted Successfully");
        System.out.println("===============================");
        System.out.println("Score      : " + result.getScore() + "/" + result.getTotalMarks());
        System.out.println("Percentage : " + String.format("%.1f%%", result.getPercentage()));
        System.out.println("Grade      : " + result.getGrade());
    }
}
