package teacher;

import model.Exam;
import model.Question;

import java.util.List;
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
                                String questionType,
                                String optA, String optB,
                                String optC, String optD,
                                String correctAnswerValue, int marks) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return null;
        }

        int questionId = generateQuestionId(exam);
        Question question = new Question();
        question.setQuestionId(questionId);
        question.setExamId(examId);

        if (!applyQuestionData(question, questionText, questionType, optA, optB, optC, optD, correctAnswerValue, marks)) {
            return null;
        }

        exam.addQuestion(question);
        persistenceHook.run();
        System.out.println("  Question Q" + questionId + " added.");
        return question;
    }

    public boolean editQuestion(String examId, int questionId,
                                String newText,
                                String questionType,
                                String newOptA, String newOptB,
                                String newOptC, String newOptD,
                                String correctAnswerValue, int newMarks) {
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
        if (!applyQuestionData(question, newText, questionType, newOptA, newOptB, newOptC, newOptD, correctAnswerValue, newMarks)) {
            return false;
        }

        exam.recalculateTotalMarks();
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
            System.out.println(number++ + ". [" + question.getQuestionType() + "] "
                + question.getQuestionText() + " [" + question.getMarks() + " mark(s)]");

            if (question.isTextBased()) {
                System.out.println("Expected answer: " + question.getCorrectAnswerDisplay());
            } else {
                List<String> options = question.getAllOptions();
                char letter = 'A';
                for (String option : options) {
                    System.out.println(letter + ") " + option);
                    letter++;
                }
                System.out.println("Correct answer: " + question.getCorrectAnswerDisplay());
            }
        }
    }

    private boolean applyQuestionData(Question question,
                                      String questionText,
                                      String questionType,
                                      String optA, String optB,
                                      String optC, String optD,
                                      String correctAnswerValue,
                                      int marks) {
        if (questionText == null || questionText.isBlank()) {
            System.out.println("  Question text is required.");
            return false;
        }
        if (marks <= 0) {
            System.out.println("  Marks must be a positive integer.");
            return false;
        }

        String normalizedType = normalizeQuestionType(questionType);
        question.setQuestionText(questionText.trim());
        question.setQuestionType(normalizedType);
        question.setMarks(marks);

        if ("TEXT".equals(normalizedType)) {
            if (correctAnswerValue == null || correctAnswerValue.isBlank()) {
                System.out.println("  A text-based question must include the expected answer.");
                return false;
            }

            question.setOptionA("");
            question.setOptionB("");
            question.setOptionC("");
            question.setOptionD("");
            question.setCorrectAnswer('A');
            question.setCorrectAnswerText(correctAnswerValue.trim());
            return true;
        }

        String cleanA = trimToEmpty(optA);
        String cleanB = trimToEmpty(optB);
        String cleanC = trimToEmpty(optC);
        String cleanD = trimToEmpty(optD);
        int optionCount = countNonBlank(cleanA, cleanB, cleanC, cleanD);

        if (optionCount < 2) {
            System.out.println("  MCQ questions must have at least 2 choices.");
            return false;
        }

        char normalizedAnswer = (correctAnswerValue == null || correctAnswerValue.isBlank())
            ? 'A'
            : Character.toUpperCase(correctAnswerValue.trim().charAt(0));

        if (!isValidOption(normalizedAnswer, cleanA, cleanB, cleanC, cleanD)) {
            System.out.println("  Correct answer must match one of the available choices.");
            return false;
        }

        question.setOptionA(cleanA);
        question.setOptionB(cleanB);
        question.setOptionC(cleanC);
        question.setOptionD(cleanD);
        question.setCorrectAnswer(normalizedAnswer);
        question.setCorrectAnswerText(String.valueOf(normalizedAnswer));
        return true;
    }

    private boolean isValidOption(char answer, String optA, String optB, String optC, String optD) {
        switch (Character.toUpperCase(answer)) {
            case 'A': return !optA.isBlank();
            case 'B': return !optB.isBlank();
            case 'C': return !optC.isBlank();
            case 'D': return !optD.isBlank();
            default:  return false;
        }
    }

    private int countNonBlank(String... values) {
        int count = 0;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeQuestionType(String questionType) {
        return "TEXT".equalsIgnoreCase(questionType) ? "TEXT" : "MCQ";
    }

    private int generateQuestionId(Exam exam) {
        return exam.getQuestions().stream()
            .mapToInt(Question::getQuestionId)
            .max()
            .orElse(0) + 1;
    }
}
