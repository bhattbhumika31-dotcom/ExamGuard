package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamGuard – model/Question.java
 * Represents either an MCQ or text-based question belonging to an Exam.
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    private int    questionId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private char   correctAnswer;      // for MCQ questions
    private String correctAnswerText;  // for text-based questions
    private String questionType;       // MCQ | TEXT
    private int    marks;
    private String examId;

    public Question() {
        this.questionType = "MCQ";
        this.correctAnswerText = "";
    }

    public Question(int questionId, String examId, String questionText,
                    String optionA, String optionB, String optionC, String optionD,
                    char correctAnswer, int marks) {
        this.questionId = questionId;
        this.examId = examId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = Character.toUpperCase(correctAnswer);
        this.correctAnswerText = String.valueOf(this.correctAnswer);
        this.questionType = "MCQ";
        this.marks = marks;
    }

    /** Returns the option text for a given letter (A/B/C/D). */
    public String getOptionText(char letter) {
        switch (Character.toUpperCase(letter)) {
            case 'A': return optionA == null ? "" : optionA;
            case 'B': return optionB == null ? "" : optionB;
            case 'C': return optionC == null ? "" : optionC;
            case 'D': return optionD == null ? "" : optionD;
            default:  return "";
        }
    }

    public boolean isCorrect(char studentAnswer) {
        return isCorrect(String.valueOf(studentAnswer));
    }

    public boolean isCorrect(String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return false;
        }

        if (isTextBased()) {
            String expected = correctAnswerText == null ? "" : correctAnswerText.trim();
            return studentAnswer.trim().equalsIgnoreCase(expected);
        }

        return Character.toUpperCase(studentAnswer.trim().charAt(0)) == Character.toUpperCase(correctAnswer);
    }

    public boolean isTextBased() {
        return "TEXT".equalsIgnoreCase(questionType);
    }

    public boolean isMcq() {
        return !isTextBased();
    }

    /** Returns only the non-empty MCQ options in display order. */
    public List<String> getAllOptions() {
        List<String> options = new ArrayList<>();
        if (isTextBased()) {
            return options;
        }
        addIfPresent(options, optionA);
        addIfPresent(options, optionB);
        addIfPresent(options, optionC);
        addIfPresent(options, optionD);
        return options;
    }

    public String getCorrectAnswerDisplay() {
        if (isTextBased()) {
            return correctAnswerText == null ? "" : correctAnswerText;
        }
        return String.valueOf(Character.toUpperCase(correctAnswer));
    }

    private void addIfPresent(List<String> options, String value) {
        if (value != null && !value.isBlank()) {
            options.add(value.trim());
        }
    }

    @Override
    public String toString() {
        return String.format("Q%d [%s | %d mark(s)]: %s", questionId, getQuestionType(), marks, questionText);
    }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public char getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(char correctAnswer) {
        this.correctAnswer = Character.toUpperCase(correctAnswer);
        if (isMcq()) {
            this.correctAnswerText = String.valueOf(this.correctAnswer);
        }
    }

    public String getCorrectAnswerText() { return correctAnswerText == null ? "" : correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText == null ? "" : correctAnswerText.trim(); }

    public String getQuestionType() { return questionType == null || questionType.isBlank() ? "MCQ" : questionType; }
    public void setQuestionType(String questionType) {
        this.questionType = (questionType == null || questionType.isBlank()) ? "MCQ" : questionType.trim().toUpperCase();
    }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
}
