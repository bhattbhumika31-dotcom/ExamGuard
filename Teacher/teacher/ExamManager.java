package teacher;

import model.Exam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExamManager {

    private Map<String, Exam> examStore;
    private int examCounter;
    private Runnable persistenceHook = () -> {};

    public ExamManager() {
        this.examStore = new LinkedHashMap<>();
        this.examCounter = 1;
    }

    public void setExamStore(Map<String, Exam> examStore) {
        this.examStore = examStore;
        this.examCounter = examStore.keySet().stream()
            .map(this::extractNumericId)
            .max(Integer::compareTo)
            .orElse(0) + 1;
    }

    public Map<String, Exam> getExamStore() {
        return examStore;
    }

    public void setPersistenceHook(Runnable persistenceHook) {
        this.persistenceHook = persistenceHook == null ? () -> {} : persistenceHook;
    }

    public Exam createExam(String title, String subject, int durationMins,
                           String teacherId, String teacherName) {
        String examId = "EXAM-" + String.format("%03d", examCounter++);
        Exam exam = new Exam(examId, title, subject, durationMins, teacherId, teacherName);
        examStore.put(examId, exam);
        persistenceHook.run();
        System.out.println("\n  Exam created successfully. ID: " + examId);
        return exam;
    }

    public List<Exam> getExamsByTeacher(String teacherId) {
        List<Exam> result = new ArrayList<>();
        for (Exam exam : examStore.values()) {
            if (teacherId.equals(exam.getCreatedByTeacherId())) {
                result.add(exam);
            }
        }
        return result;
    }

    public Exam getExamById(String examId) {
        return examStore.get(examId);
    }

    public boolean editExam(String examId, String newTitle, String newSubject, int newDurationMins) {
        Exam exam = examStore.get(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return false;
        }

        if (newTitle != null && !newTitle.isBlank()) {
            exam.setTitle(newTitle);
        }
        if (newSubject != null && !newSubject.isBlank()) {
            exam.setSubject(newSubject);
        }
        if (newDurationMins > 0) {
            exam.setDurationMinutes(newDurationMins);
        }

        persistenceHook.run();
        System.out.println("  Exam updated: " + examId);
        return true;
    }

    public boolean toggleExamStatus(String examId) {
        Exam exam = examStore.get(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return false;
        }

        exam.setActive(!exam.isActive());
        persistenceHook.run();
        System.out.println("  Exam [" + examId + "] is now "
            + (exam.isActive() ? "ACTIVE." : "INACTIVE."));
        return true;
    }

    public boolean deleteExam(String examId, String teacherId) {
        Exam exam = examStore.get(examId);
        if (exam == null) {
            System.out.println("  Exam not found: " + examId);
            return false;
        }
        if (!teacherId.equals(exam.getCreatedByTeacherId())) {
            System.out.println("  Permission denied. You did not create this exam.");
            return false;
        }

        examStore.remove(examId);
        persistenceHook.run();
        System.out.println("  Exam deleted: " + examId);
        return true;
    }

    private int extractNumericId(String examId) {
        int dashIndex = examId.lastIndexOf('-');
        if (dashIndex < 0 || dashIndex == examId.length() - 1) {
            return 0;
        }

        try {
            return Integer.parseInt(examId.substring(dashIndex + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
