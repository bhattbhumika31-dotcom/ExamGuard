package core;

import model.Exam;
import model.Result;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared repository for the teacher and student modules.
 * It keeps the same stores in memory and persists them to disk
 * so both modules can work against the same data.
 */
public final class ExamGuardRepository {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path EXAMS_FILE = DATA_DIR.resolve("exams.ser");
    private static final Path RESULTS_FILE = DATA_DIR.resolve("results.ser");

    private static final ExamGuardRepository INSTANCE = new ExamGuardRepository();

    private Map<String, Exam> examStore;
    private List<Result> resultStore;

    private ExamGuardRepository() {
        this.examStore = loadExams();
        this.resultStore = loadResults();
    }

    public static ExamGuardRepository getInstance() {
        return INSTANCE;
    }

    public synchronized Map<String, Exam> getExamStore() {
        return examStore;
    }

    public synchronized List<Result> getResultStore() {
        return resultStore;
    }

    public synchronized List<Exam> getActiveExams() {
        return examStore.values().stream()
            .filter(Exam::isActive)
            .sorted(Comparator.comparing(Exam::getExamId))
            .collect(Collectors.toList());
    }

    public synchronized Exam findExam(String examId) {
        return examStore.get(examId);
    }

    public synchronized void persistExams() {
        saveObject(EXAMS_FILE, examStore);
    }

    public synchronized void persistResults() {
        saveObject(RESULTS_FILE, resultStore);
    }

    public synchronized boolean addOrUpdateResult(Result result) {
        if (result == null) {
            return false;
        }
        if (hasResultForStudentExam(result.getStudentId(), result.getExamId())) {
            return false;
        }
        resultStore.add(result);
        persistResults();
        DatabaseAuthenticator.saveStudentMark(result);
        return true;
    }

    public synchronized List<Result> getResultsForStudent(String studentId) {
        return resultStore.stream()
            .filter(result -> result.getStudentId().equals(studentId))
            .sorted(Comparator.comparing(Result::getAttemptDate).reversed())
            .collect(Collectors.toList());
    }

    public synchronized boolean hasResultForStudentExam(String studentId, String examId) {
        return findResultForStudentExam(studentId, examId) != null;
    }

    public synchronized Result findResultForStudentExam(String studentId, String examId) {
        if (studentId == null || studentId.isBlank() || examId == null || examId.isBlank()) {
            return null;
        }

        return resultStore.stream()
            .filter(result -> studentId.equals(result.getStudentId()) && examId.equals(result.getExamId()))
            .findFirst()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Exam> loadExams() {
        Object value = loadObject(EXAMS_FILE);
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Exam>) map);
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Result> loadResults() {
        Object value = loadObject(RESULTS_FILE);
        if (value instanceof List<?> list) {
            return new ArrayList<>((List<Result>) list);
        }
        return new ArrayList<>();
    }

    private Object loadObject(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
            return input.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            return null;
        }
    }

    private void saveObject(Path path, Object value) {
        try {
            Files.createDirectories(DATA_DIR);
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))) {
                output.writeObject(value);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to persist ExamGuard data.", ex);
        }
    }
}
