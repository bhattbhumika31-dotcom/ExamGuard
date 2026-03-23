package model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * ExamGuard – model/Question.java
 * Represents a single MCQ question belonging to an Exam.
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
    private char   correctAnswer;  // 'A' | 'B' | 'C' | 'D'
    private int    marks;
    private String examId;         // foreign key to Exam

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Question() {}

    public Question(int questionId, String examId, String questionText,
                    String optionA, String optionB, String optionC, String optionD,
                    char correctAnswer, int marks) {
        this.questionId    = questionId;
        this.examId        = examId;
        this.questionText  = questionText;
        this.optionA       = optionA;
        this.optionB       = optionB;
        this.optionC       = optionC;
        this.optionD       = optionD;
        this.correctAnswer = Character.toUpperCase(correctAnswer);
        this.marks         = marks;
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    /** Returns the option text for a given letter (A/B/C/D). */
    public String getOptionText(char letter) {
        switch (Character.toUpperCase(letter)) {
            case 'A': return optionA;
            case 'B': return optionB;
            case 'C': return optionC;
            case 'D': return optionD;
            default:  return "Invalid option";
        }
    }

    public boolean isCorrect(char studentAnswer) {
        return Character.toUpperCase(studentAnswer) == correctAnswer;
    }

    /** Returns options as an ordered list [A, B, C, D]. */
    public List<String> getAllOptions() {
        return Arrays.asList(optionA, optionB, optionC, optionD);
    }

    @Override
    public String toString() {
        return String.format("Q%d [%d mark(s)]: %s", questionId, marks, questionText);
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public int    getQuestionId()                       { return questionId; }
    public void   setQuestionId(int questionId)         { this.questionId = questionId; }

    public String getQuestionText()                     { return questionText; }
    public void   setQuestionText(String questionText)  { this.questionText = questionText; }

    public String getOptionA()                          { return optionA; }
    public void   setOptionA(String optionA)            { this.optionA = optionA; }

    public String getOptionB()                          { return optionB; }
    public void   setOptionB(String optionB)            { this.optionB = optionB; }

    public String getOptionC()                          { return optionC; }
    public void   setOptionC(String optionC)            { this.optionC = optionC; }

    public String getOptionD()                          { return optionD; }
    public void   setOptionD(String optionD)            { this.optionD = optionD; }

    public char   getCorrectAnswer()                    { return correctAnswer; }
    public void   setCorrectAnswer(char correctAnswer)  { this.correctAnswer = Character.toUpperCase(correctAnswer); }

    public int    getMarks()                            { return marks; }
    public void   setMarks(int marks)                   { this.marks = marks; }

    public String getExamId()                           { return examId; }
    public void   setExamId(String examId)              { this.examId = examId; }
}
