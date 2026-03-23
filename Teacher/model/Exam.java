package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamGuard – model/Exam.java
 * Represents a single exam created by a Teacher.
 * Implements Serializable so the Core module can save/recover it.
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String examId;
    private String title;
    private String subject;
    private int durationMinutes;
    private int totalMarks;           // auto-computed from questions
    private String createdByTeacherId;
    private String createdByTeacherName;
    private List<Question> questions;
    private boolean active;            // true = students can attempt it

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Exam() {
        this.questions = new ArrayList<>();
        this.active    = false;
    }

    public Exam(String examId, String title, String subject,
                int durationMinutes,
                String createdByTeacherId, String createdByTeacherName) {
        this.examId                = examId;
        this.title                 = title;
        this.subject               = subject;
        this.durationMinutes       = durationMinutes;
        this.createdByTeacherId    = createdByTeacherId;
        this.createdByTeacherName  = createdByTeacherName;
        this.questions             = new ArrayList<>();
        this.active                = false;
        this.totalMarks            = 0;
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    /** Recalculates totalMarks by summing marks of all questions. */
    public void recalculateTotalMarks() {
        this.totalMarks = questions.stream()
                                   .mapToInt(Question::getMarks)
                                   .sum();
    }

    public void addQuestion(Question q) {
        questions.add(q);
        recalculateTotalMarks();
    }

    public boolean removeQuestion(int questionId) {
        boolean removed = questions.removeIf(q -> q.getQuestionId() == questionId);
        if (removed) recalculateTotalMarks();
        return removed;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) | %d min | %d marks | Active: %s",
                examId, title, subject, durationMinutes, totalMarks, active);
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public String getExamId()                        { return examId; }
    public void   setExamId(String examId)           { this.examId = examId; }

    public String getTitle()                         { return title; }
    public void   setTitle(String title)             { this.title = title; }

    public String getSubject()                       { return subject; }
    public void   setSubject(String subject)         { this.subject = subject; }

    public int  getDurationMinutes()                 { return durationMinutes; }
    public void setDurationMinutes(int d)            { this.durationMinutes = d; }

    public int  getTotalMarks()                      { return totalMarks; }

    public String getCreatedByTeacherId()            { return createdByTeacherId; }
    public void   setCreatedByTeacherId(String id)   { this.createdByTeacherId = id; }

    public String getCreatedByTeacherName()          { return createdByTeacherName; }
    public void   setCreatedByTeacherName(String n)  { this.createdByTeacherName = n; }

    public List<Question> getQuestions()             { return questions; }
    public void           setQuestions(List<Question> q) { this.questions = q; recalculateTotalMarks(); }

    public boolean isActive()                        { return active; }
    public void    setActive(boolean active)         { this.active = active; }
}
