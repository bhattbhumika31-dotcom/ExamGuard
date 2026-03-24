package teacher;

import model.Exam;
import model.Result;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ExamGuard – teacher/AnalyticsDashboard.java
 *
 * Provides the Exam Records / Analytics view for a Teacher:
 *   • Summary stats per exam (highest, lowest, average score)
 *   • Full result roster for an exam
 *   • Pass / fail breakdown
 *   • Grade distribution
 *   • Search a student's result
 *
 * The result list is shared / injected by the Core module.
 * All computation is read-only; no data is modified here.
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class AnalyticsDashboard {

    /** Injected by Core module – list of all exam results. */
    private List<Result> resultStore;
    private final ExamManager examManager;

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public AnalyticsDashboard(ExamManager examManager) {
        this.examManager = examManager;
        this.resultStore = new ArrayList<>();
    }

    /** Core module injects the shared result list here. */
    public void setResultStore(List<Result> resultStore) {
        this.resultStore = resultStore;
    }

    // ------------------------------------------------------------------ //
    //  1. OVERVIEW DASHBOARD  (all exams by this teacher)
    // ------------------------------------------------------------------ //

    /**
     * Prints a one-line summary per exam created by the given teacher.
     */
    public void showOverview(String teacherId) {
        List<Exam> myExams = examManager.getExamsByTeacher(teacherId);
        if (myExams.isEmpty()) {
            System.out.println("\n  No exams found. Create an exam first.");
            return;
        }

        System.out.println("\n  +----------+-------------------------+----------+--------+------+------+");
        System.out.println(  "  | Exam ID  | Title                   | Attempts | Avg %  | Pass | Fail |");
        System.out.println(  "  +----------+-------------------------+----------+--------+------+------+");

        for (Exam exam : myExams) {
            List<Result> rs = getResultsForExam(exam.getExamId());
            long pass = rs.stream().filter(Result::isPassed).count();
            long fail = rs.size() - pass;
            double avg = rs.stream().mapToDouble(Result::getPercentage).average().orElse(0.0);

            System.out.printf("  | %-8s | %-23s | %-8d | %5.1f%% | %-4d | %-4d |%n",
                    exam.getExamId(),
                    truncate(exam.getTitle(), 23),
                    rs.size(), avg, pass, fail);
        }
        System.out.println(  "  +----------+-------------------------+----------+--------+------+------+");
    }

    // ------------------------------------------------------------------ //
    //  2. DETAILED RESULT ROSTER  (one exam)
    // ------------------------------------------------------------------ //

    public void showResultRoster(String examId) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  [ERROR] Exam not found: " + examId);
            return;
        }

        List<Result> rs = getResultsForExam(examId);
        if (rs.isEmpty()) {
            System.out.println("\n  No students have attempted exam [" + examId + "] yet.");
            return;
        }

        System.out.println("\n  ===========================================================================");
        System.out.println(  "   Result Roster: " + exam.getTitle() + "  [" + examId + "]");
        System.out.println(  "  ===========================================================================");
        System.out.printf(   "  %-5s %-20s %-12s %-8s %-6s %-6s%n",
                "#", "Student Name", "Score", "Percent", "Grade", "Status");
        System.out.println(  "  ---------------------------------------------------------------------------");

        // Sort by percentage descending (rank order)
        rs.sort(Comparator.comparingDouble(Result::getPercentage).reversed());

        int rank = 1;
        for (Result r : rs) {
            System.out.printf("  %-5d %-20s %3d/%-5d %5.1f%%   %-6s %s%n",
                    rank++,
                    truncate(r.getStudentName(), 20),
                    r.getScore(), r.getTotalMarks(),
                    r.getPercentage(),
                    r.getGrade(),
                    r.isPassed() ? "PASS" : "FAIL");
        }
        System.out.println(  "  ===========================================================================");
        printExamStats(rs);
    }

    // ------------------------------------------------------------------ //
    //  3. STATISTICS BLOCK
    // ------------------------------------------------------------------ //

    private void printExamStats(List<Result> rs) {
        if (rs.isEmpty()) return;

        DoubleSummaryStatistics stats =
                rs.stream().mapToDouble(Result::getPercentage).summaryStatistics();
        long passed = rs.stream().filter(Result::isPassed).count();

        System.out.println("\n  +------------------------------------+");
        System.out.println(  "  |         EXAM STATISTICS            |");
        System.out.println(  "  +------------------------------------+");
        System.out.printf(   "  |  Total Attempts : %-16d|%n", rs.size());
        System.out.printf(   "  |  Highest Score  : %15.1f%% |%n", stats.getMax());
        System.out.printf(   "  |  Lowest Score   : %15.1f%% |%n", stats.getMin());
        System.out.printf(   "  |  Average Score  : %15.1f%% |%n", stats.getAverage());
        System.out.printf(   "  |  Passed         : %-16d|%n", passed);
        System.out.printf(   "  |  Failed         : %-16d|%n", rs.size() - passed);
        System.out.printf(   "  |  Pass Rate      : %15.1f%% |%n",
                (passed * 100.0 / rs.size()));
        System.out.println(  "  +------------------------------------+");
    }

    // ------------------------------------------------------------------ //
    //  4. GRADE DISTRIBUTION  (ASCII bar chart)
    // ------------------------------------------------------------------ //

    public void showGradeDistribution(String examId) {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            System.out.println("  [ERROR] Exam not found: " + examId);
            return;
        }

        List<Result> rs = getResultsForExam(examId);
        if (rs.isEmpty()) {
            System.out.println("\n  No results available for: " + examId);
            return;
        }

        Map<String, Long> dist = rs.stream()
                .collect(Collectors.groupingBy(Result::getGrade, Collectors.counting()));

        String[] grades = {"A+", "A", "B", "C", "D", "F"};
        int total = rs.size();

        System.out.println("\n  Grade Distribution -- " + exam.getTitle());
        System.out.println("  ------------------------------------------");
        for (String g : grades) {
            long count = dist.getOrDefault(g, 0L);
            int  bar   = total > 0 ? (int) (count * 30.0 / total) : 0;
            System.out.printf("  %2s | %-30s | %3d student(s)%n",
                    g, "#".repeat(bar), count);
        }
        System.out.println("  ------------------------------------------");
    }

    // ------------------------------------------------------------------ //
    //  5. SEARCH STUDENT RESULT
    // ------------------------------------------------------------------ //

    public void searchStudentResult(String examId, String studentName) {
        List<Result> rs = getResultsForExam(examId).stream()
                .filter(r -> r.getStudentName()
                              .toLowerCase()
                              .contains(studentName.toLowerCase()))
                .collect(Collectors.toList());

        if (rs.isEmpty()) {
            System.out.println("  No results found for student: " + studentName);
            return;
        }
        System.out.println("\n  Search Results:");
        rs.forEach(r -> System.out.println("  " + r));
    }

    // ------------------------------------------------------------------ //
    //  Utility
    // ------------------------------------------------------------------ //

    private List<Result> getResultsForExam(String examId) {
        return resultStore.stream()
                .filter(r -> r.getExamId().equals(examId))
                .collect(Collectors.toList());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "~";
    }
}
