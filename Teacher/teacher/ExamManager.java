package teacher;

import model.Exam;
import model.Question;

import java.util.*;

/**
 * ExamGuard – teacher/ExamManager.java
 *
 * Handles all exam-level CRUD operations for the Teacher:
 *   • Create a new exam
 *   • Edit exam metadata (title, subject, duration)
 *   • Activate / deactivate an exam
 *   • Delete an exam
 *   • List all exams created by this teacher
 *
 * Storage is kept in an in-memory Map<examId, Exam>.
 * The Core module (Parth) will inject / replace this store via
 * setExamStore() once file-based persistence is ready.
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class ExamManager {

    // ------------------------------------------------------------------ //
    //  State – injected / shared with Core module
    // ------------------------------------------------------------------ //

    /** Shared exam store: examId → Exam.  Injected by Core. */
    private Map<String, Exam> examStore;

    /** Counter used to generate unique exam IDs. */
    private int examCounter;

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public ExamManager() {
        this.examStore   = new LinkedHashMap<>();
        this.examCounter = 1;
    }

    /** Core module injects its persistent store here. */
    public void setExamStore(Map<String, Exam> examStore) {
        this.examStore = examStore;
        // keep counter ahead of existing IDs
        this.examCounter = examStore.size() + 1;
    }

    public Map<String, Exam> getExamStore() {
        return examStore;
    }

    // ------------------------------------------------------------------ //
    //  CREATE
    // ------------------------------------------------------------------ //

    /**
     * Creates a new Exam and adds it to the store.
     *
     * @param title          Exam title
     * @param subject        Subject name
     * @param durationMins   Allowed time in minutes
     * @param teacherId      ID of the logged-in teacher
     * @param teacherName    Name of the logged-in teacher
     * @return the newly created Exam
     */
    public Exam createExam(String title, String subject, int durationMins,
                           String teacherId, String teacherName) {
        String examId = "EXAM-" + String.format("%03d", examCounter++);
        Exam exam = new Exam(examId, title, subject, durationMins, teacherId, teacherName);
        examStore.put(examId, exam);
        System.out.println("\n  ✔ Exam created successfully! ID: " + examId);
        return exam;
    }

    // ------------------------------------------------------------------ //
    //  READ
    // ------------------------------------------------------------------ //

    /**
     * Returns all exams created by a specific teacher.
     */
    public List<Exam> getExamsByTeacher(String teacherId) {
        List<Exam> result = new ArrayList<>();
        for (Exam e : examStore.values()) {
            if (e.getCreatedByTeacherId().equals(teacherId)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Fetches a single exam by ID.
     * Returns null if not found.
     */
    public Exam getExamById(String examId) {
        return examStore.get(examId);
    }

    /**
     * Displays all exams belonging to the teacher in a formatted table.
     */
    public void listExams(String teacherId) {
        List<Exam> exams = getExamsByTeacher(teacherId);
        if (exams.isEmpty()) {
            System.out.println("\n  No exams found. Create one first.");
            return;
        }
        System.out.println("\n  ┌──────────────────────────────────────────────────────────────────────┐");
        System.out.println(  "  │                        YOUR EXAM LIST                               │");
        System.out.println(  "  ├───────────┬─────────────────────────┬────────────┬──────┬──────────┤");
        System.out.printf(   "  │ %-9s │ %-23s │ %-10s │ %-4s │ %-8s │%n",
                            "ID", "Title", "Subject", "Mins", "Active");
        System.out.println(  "  ├───────────┼─────────────────────────┼────────────┼──────┼──────────┤");
        for (Exam e : exams) {
            System.out.printf("  │ %-9s │ %-23s │ %-10s │ %-4d │ %-8s │%n",
                    e.getExamId(),
                    truncate(e.getTitle(), 23),
                    truncate(e.getSubject(), 10),
                    e.getDurationMinutes(),
                    e.isActive() ? "YES" : "NO");
        }
        System.out.println(  "  └───────────┴─────────────────────────┴────────────┴──────┴──────────┘");
    }

    // ------------------------------------------------------------------ //
    //  UPDATE
    // ------------------------------------------------------------------ //

    /**
     * Edits the metadata of an existing exam.
     * Pass null / -1 for fields that should remain unchanged.
     */
    public boolean editExam(String examId, String newTitle, String newSubject,
                            int newDurationMins) {
        Exam exam = examStore.get(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return false;
        }
        if (newTitle        != null && !newTitle.isBlank())   exam.setTitle(newTitle);
        if (newSubject      != null && !newSubject.isBlank()) exam.setSubject(newSubject);
        if (newDurationMins >  0)                             exam.setDurationMinutes(newDurationMins);

        System.out.println("  ✔ Exam updated: " + examId);
        return true;
    }

    /**
     * Toggles the active status of an exam.
     * Active exams are visible to students for attempting.
     */
    public boolean toggleExamStatus(String examId) {
        Exam exam = examStore.get(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return false;
        }
        exam.setActive(!exam.isActive());
        System.out.println("  ✔ Exam [" + examId + "] is now "
                + (exam.isActive() ? "ACTIVE (students can attempt it)."
                                   : "INACTIVE (hidden from students)."));
        return true;
    }

    // ------------------------------------------------------------------ //
    //  DELETE
    // ------------------------------------------------------------------ //

    /**
     * Deletes an exam from the store.
     * Only allowed if exam belongs to the requesting teacher.
     */
    public boolean deleteExam(String examId, String teacherId) {
        Exam exam = examStore.get(examId);
        if (exam == null) {
            System.out.println("  ✘ Exam not found: " + examId);
            return false;
        }
        if (!exam.getCreatedByTeacherId().equals(teacherId)) {
            System.out.println("  ✘ Permission denied. You did not create this exam.");
            return false;
        }
        examStore.remove(examId);
        System.out.println("  ✔ Exam deleted: " + examId);
        return true;
    }

    // ------------------------------------------------------------------ //
    //  Utility
    // ------------------------------------------------------------------ //

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
