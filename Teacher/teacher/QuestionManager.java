package teacher;

import model.Exam;
import model.Question;

import java.util.*;

/**
 * ExamGuard – teacher/QuestionManager.java
 *
 * Manages individual questions inside an exam:
 *   • Add question (MCQ with 4 options)
 *   • Edit question text / options / correct answer / marks
 *   • Delete question by ID
 *   • List all questions of an exam
 *   • Preview exam as a student would see it
 *
 * Works directly on the Exam object (questions are embedded in the Exam).
 * The Core module handles persistence; this class is pure logic.
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class QuestionManager {

    /** Running counter for auto-generating question IDs within a session. */
    private int questionCounter;

    private final ExamManager examManager;

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public QuestionManager(ExamManager examManager) {
        this.examManager     = examManager;
        this.questionCounter = 1;
    }

    // ------------------------------------------------------------------ //
    //  ADD QUESTION
    // ------------------------------------------------------------------ //

    /**
     * Adds a new MCQ question to the specified exam.
     *
     * @param examId        target exam
     * @param questionText  the question stem
     * @param optA–optD     the four choices
     * @param correctAnswer 'A' | 'B' | 'C' | 'D'
     * @param marks         marks awarded for a correct answer
     * @return the created Question, or null on failure
     */
    public Question addQuestion(String examId,
                                String questionText,
                                String optA, String optB,
                                String optC, String optD,
                                char correctAnswer, int marks) {

        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return null;
        }

        char ca = Character.toUpperCase(correctAnswer);
        if (ca != 'A' && ca != 'B' && ca != 'C' && ca != 'D') {
            System.out.println("  ✘ Correct answer must be A, B, C, or D.");
            return null;
        }

        if (marks <= 0) {
            System.out.println("  ✘ Marks must be a positive integer.");
            return null;
        }

        int qId = generateQuestionId(exam);
        Question q = new Question(qId, examId, questionText,
                                  optA, optB, optC, optD,
                                  ca, marks);
        exam.addQuestion(q);

        System.out.println("  ✔ Question Q" + qId + " added. "
                + "Exam total now: " + exam.getTotalMarks() + " marks.");
        return q;
    }

    // ------------------------------------------------------------------ //
    //  READ / LIST
    // ------------------------------------------------------------------ //

    /**
     * Prints all questions of an exam in a readable format.
     */
    public void listQuestions(String examId) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return;
        }

        List<Question> qs = exam.getQuestions();
        if (qs.isEmpty()) {
            System.out.println("\n  No questions added yet for exam: " + examId);
            return;
        }

        System.out.println("\n  ══════════════════════════════════════════════════════");
        System.out.println(  "   Questions for: " + exam.getTitle()
                           + " [" + examId + "]  |  Total Marks: " + exam.getTotalMarks());
        System.out.println(  "  ══════════════════════════════════════════════════════");

        for (Question q : qs) {
            System.out.printf("%n  Q%-3d [%d mark(s)] %s%n", q.getQuestionId(), q.getMarks(), q.getQuestionText());
            System.out.printf("       A) %s%n", q.getOptionA());
            System.out.printf("       B) %s%n", q.getOptionB());
            System.out.printf("       C) %s%n", q.getOptionC());
            System.out.printf("       D) %s%n", q.getOptionD());
            System.out.printf("       ✔ Correct: %s) %s%n",
                    q.getCorrectAnswer(), q.getOptionText(q.getCorrectAnswer()));
        }
        System.out.println("\n  ══════════════════════════════════════════════════════");
    }

    // ------------------------------------------------------------------ //
    //  EDIT QUESTION
    // ------------------------------------------------------------------ //

    /**
     * Updates fields of an existing question.
     * Pass null / '\0' / -1 for fields that should remain unchanged.
     */
    public boolean editQuestion(String examId, int questionId,
                                String newText,
                                String newOptA, String newOptB,
                                String newOptC, String newOptD,
                                char newCorrectAnswer, int newMarks) {

        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return false;
        }

        Optional<Question> found = exam.getQuestions().stream()
                .filter(q -> q.getQuestionId() == questionId)
                .findFirst();

        if (found.isEmpty()) {
            System.out.println("  ✘ Question Q" + questionId + " not found in exam " + examId);
            return false;
        }

        Question q = found.get();

        if (newText    != null && !newText.isBlank())    q.setQuestionText(newText);
        if (newOptA    != null && !newOptA.isBlank())    q.setOptionA(newOptA);
        if (newOptB    != null && !newOptB.isBlank())    q.setOptionB(newOptB);
        if (newOptC    != null && !newOptC.isBlank())    q.setOptionC(newOptC);
        if (newOptD    != null && !newOptD.isBlank())    q.setOptionD(newOptD);
        if (newMarks   >  0)                             { q.setMarks(newMarks); exam.recalculateTotalMarks(); }

        char ca = Character.toUpperCase(newCorrectAnswer);
        if (ca == 'A' || ca == 'B' || ca == 'C' || ca == 'D') {
            q.setCorrectAnswer(ca);
        }

        System.out.println("  ✔ Question Q" + questionId + " updated successfully.");
        return true;
    }

    // ------------------------------------------------------------------ //
    //  DELETE QUESTION
    // ------------------------------------------------------------------ //

    public boolean deleteQuestion(String examId, int questionId) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return false;
        }
        boolean removed = exam.removeQuestion(questionId);
        if (removed) {
            System.out.println("  ✔ Question Q" + questionId + " removed. "
                    + "Exam total now: " + exam.getTotalMarks() + " marks.");
        } else {
            System.out.println("  ✘ Question Q" + questionId + " not found.");
        }
        return removed;
    }

    // ------------------------------------------------------------------ //
    //  PREVIEW (student view, without correct answers)
    // ------------------------------------------------------------------ //

    public void previewExam(String examId) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return;
        }
        System.out.println("\n  ╔══════════════════════════════════════════════════════╗");
        System.out.printf (  "  ║  EXAM PREVIEW: %-38s║%n", exam.getTitle());
        System.out.printf (  "  ║  Subject: %-43s║%n", exam.getSubject());
        System.out.printf (  "  ║  Duration: %d min  |  Total Marks: %-20s║%n",
                             exam.getDurationMinutes(), exam.getTotalMarks());
        System.out.println(  "  ╚══════════════════════════════════════════════════════╝");

        int num = 1;
        for (Question q : exam.getQuestions()) {
            System.out.printf("%n  %d. %s  [%d mark(s)]%n", num++, q.getQuestionText(), q.getMarks());
            System.out.printf("     A) %s%n", q.getOptionA());
            System.out.printf("     B) %s%n", q.getOptionB());
            System.out.printf("     C) %s%n", q.getOptionC());
            System.out.printf("     D) %s%n", q.getOptionD());
        }
        System.out.println("\n  [End of Preview]");
    }

    // ------------------------------------------------------------------ //
    //  Utility
    // ------------------------------------------------------------------ //

    /** Generates the next unique question ID within the exam. */
    private int generateQuestionId(Exam exam) {
        return exam.getQuestions().stream()
                   .mapToInt(Question::getQuestionId)
                   .max()
                   .orElse(0) + 1;
    }
}
