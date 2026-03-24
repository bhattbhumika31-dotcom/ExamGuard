package teacher;

import model.Exam;
import model.Question;

import java.util.Optional;

public class QuestionManager {

    private final ExamManager examManager;
    private Runnable persistenceHook = () -> {};

    public QuestionManager(ExamManager examManager) {
        this.examManager = examManager;
    }

    public void setPersistenceHook(Runnable persistenceHook) {
        this.persistenceHook = persistenceHook == null ? () -> {} : persistenceHook;
    }

    public Question addQuestion(String examId,
                                String questionText,
                                String optA, String optB,
                                String optC, String optD,
                                char correctAnswer, int marks) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return null;
        }

        char normalizedAnswer = Character.toUpperCase(correctAnswer);
        if (normalizedAnswer != 'A' && normalizedAnswer != 'B'
                && normalizedAnswer != 'C' && normalizedAnswer != 'D') {
            System.out.println("  Correct answer must be A, B, C, or D.");
            return null;
        }
        if (marks <= 0) {
            System.out.println("  Marks must be a positive integer.");
            return null;
        }

        int questionId = generateQuestionId(exam);
        Question question = new Question(
            questionId, examId, questionText, optA, optB, optC, optD, normalizedAnswer, marks);
        exam.addQuestion(question);
        persistenceHook.run();

        System.out.println("  Question Q" + questionId + " added.");
        return question;
    }

    public boolean editQuestion(String examId, int questionId,
                                String newText,
                                String newOptA, String newOptB,
                                String newOptC, String newOptD,
                                char newCorrectAnswer, int newMarks) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return false;
        }

        Optional<Question> found = exam.getQuestions().stream()
            .filter(question -> question.getQuestionId() == questionId)
            .findFirst();
        if (found.isEmpty()) {
            System.out.println("  Question Q" + questionId + " not found in exam " + examId);
            return false;
        }

        Question question = found.get();
        if (newText != null && !newText.isBlank()) {
            question.setQuestionText(newText);
        }
        if (newOptA != null && !newOptA.isBlank()) {
            question.setOptionA(newOptA);
        }
        if (newOptB != null && !newOptB.isBlank()) {
            question.setOptionB(newOptB);
        }
        if (newOptC != null && !newOptC.isBlank()) {
            question.setOptionC(newOptC);
        }
        if (newOptD != null && !newOptD.isBlank()) {
            question.setOptionD(newOptD);
        }
        if (newMarks > 0) {
            question.setMarks(newMarks);
            exam.recalculateTotalMarks();
        }

        char normalizedAnswer = Character.toUpperCase(newCorrectAnswer);
        if (normalizedAnswer == 'A' || normalizedAnswer == 'B'
                || normalizedAnswer == 'C' || normalizedAnswer == 'D') {
            question.setCorrectAnswer(normalizedAnswer);
        }

        persistenceHook.run();
        System.out.println("  Question Q" + questionId + " updated successfully.");
        return true;
    }

    public boolean deleteQuestion(String examId, int questionId) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return false;
        }

        boolean removed = exam.removeQuestion(questionId);
        if (removed) {
            persistenceHook.run();
            System.out.println("  Question Q" + questionId + " removed.");
        } else {
            System.out.println("  Question Q" + questionId + " not found.");
        }
        return removed;
    }

    public void previewExam(String examId) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return;
        }

        System.out.println("\nExam Preview: " + exam.getTitle());
        System.out.println("Subject: " + exam.getSubject());
        System.out.println("Duration: " + exam.getDurationMinutes() + " min");
        System.out.println("Total Marks: " + exam.getTotalMarks());

        int number = 1;
        for (Question question : exam.getQuestions()) {
            System.out.println();
            System.out.println(number++ + ". " + question.getQuestionText()
                + " [" + question.getMarks() + " mark(s)]");
            System.out.println("A) " + question.getOptionA());
            System.out.println("B) " + question.getOptionB());
            System.out.println("C) " + question.getOptionC());
            System.out.println("D) " + question.getOptionD());
        }
    }

    private int generateQuestionId(Exam exam) {
        return exam.getQuestions().stream()
            .mapToInt(Question::getQuestionId)
            .max()
            .orElse(0) + 1;
    }
}
