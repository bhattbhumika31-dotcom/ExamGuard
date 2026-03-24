package model;

import java.io.Serializable;

/**
 * ExamGuard – model/Result.java
 * Stores the outcome of a student's exam attempt.
 * Used by both the Student module (to view results) and the
 * Teacher module (analytics dashboard).
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    private String resultId;
    private String studentId;
    private String studentName;
    private String examId;
    private String examTitle;
    private int    score;
    private int    totalMarks;
    private double percentage;
    private String grade;
    private long   timeTakenSeconds;
    private String attemptDate;   // "dd-MM-yyyy HH:mm"

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Result() {}

    public Result(String resultId, String studentId, String studentName,
                  String examId, String examTitle,
                  int score, int totalMarks, long timeTakenSeconds, String attemptDate) {
        this.resultId         = resultId;
        this.studentId        = studentId;
        this.studentName      = studentName;
        this.examId           = examId;
        this.examTitle        = examTitle;
        this.score            = score;
        this.totalMarks       = totalMarks;
        this.timeTakenSeconds = timeTakenSeconds;
        this.attemptDate      = attemptDate;
        computeDerivedFields();
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    public void computeDerivedFields() {
        this.percentage = (totalMarks > 0) ? (score * 100.0 / totalMarks) : 0.0;
        this.grade      = computeGrade(percentage);
    }

    private String computeGrade(double pct) {
        if (pct >= 90) return "A+";
        if (pct >= 75) return "A";
        if (pct >= 60) return "B";
        if (pct >= 45) return "C";
        if (pct >= 33) return "D";
        return "F";
    }

    public boolean isPassed() {
        return percentage >= 33.0;
    }

    @Override
    public String toString() {
        return String.format("%-20s | %-25s | %3d/%-3d | %5.1f%% | Grade: %2s | %s",
                studentName, examTitle, score, totalMarks,
                percentage, grade, attemptDate);
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public String getResultId()                      { return resultId; }
    public void   setResultId(String resultId)       { this.resultId = resultId; }

    public String getStudentId()                     { return studentId; }
    public void   setStudentId(String studentId)     { this.studentId = studentId; }

    public String getStudentName()                   { return studentName; }
    public void   setStudentName(String studentName) { this.studentName = studentName; }

    public String getExamId()                        { return examId; }
    public void   setExamId(String examId)           { this.examId = examId; }

    public String getExamTitle()                     { return examTitle; }
    public void   setExamTitle(String examTitle)     { this.examTitle = examTitle; }

    public int    getScore()                         { return score; }
    public void   setScore(int score)                { this.score = score; computeDerivedFields(); }

    public int    getTotalMarks()                    { return totalMarks; }
    public void   setTotalMarks(int totalMarks)      { this.totalMarks = totalMarks; computeDerivedFields(); }

    public double getPercentage()                    { return percentage; }

    public String getGrade()                         { return grade; }

    public long   getTimeTakenSeconds()              { return timeTakenSeconds; }
    public void   setTimeTakenSeconds(long t)        { this.timeTakenSeconds = t; }

    public String getAttemptDate()                   { return attemptDate; }
    public void   setAttemptDate(String attemptDate) { this.attemptDate = attemptDate; }
}
