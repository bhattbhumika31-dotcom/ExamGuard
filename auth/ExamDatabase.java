package auth;

import model.Exam;
import model.Question;
import model.Result;

import java.io.*;
import java.util.*;

public class ExamDatabase {
    private static ExamDatabase instance;
    private Map<String, Exam> exams;
    private List<Question> questions;
    private List<Result> results;
    
    private static final String EXAMS_FILE = "exams.db";
    private static final String QUESTIONS_FILE = "questions.db";
    private static final String RESULTS_FILE = "results.db";

    private ExamDatabase() {
        exams = new HashMap<>();
        questions = new ArrayList<>();
        results = new ArrayList<>();
        loadAllData();
    }

    public static synchronized ExamDatabase getInstance() {
        if (instance == null) {
            instance = new ExamDatabase();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadAllData() {
        loadExams();
        loadQuestions();
        loadResults();
    }

    @SuppressWarnings("unchecked")
    private void loadExams() {
        File file = new File(EXAMS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                exams = (Map<String, Exam>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading exams: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadQuestions() {
        File file = new File(QUESTIONS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                questions = (List<Question>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading questions: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadResults() {
        File file = new File(RESULTS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                results = (List<Result>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading results: " + e.getMessage());
            }
        }
    }

    public void saveExams() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(EXAMS_FILE))) {
            oos.writeObject(exams);
        } catch (IOException e) {
            System.err.println("Error saving exams: " + e.getMessage());
        }
    }

    public void saveQuestions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(QUESTIONS_FILE))) {
            oos.writeObject(questions);
        } catch (IOException e) {
            System.err.println("Error saving questions: " + e.getMessage());
        }
    }

    public void saveResults() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RESULTS_FILE))) {
            oos.writeObject(results);
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }

    // Exam operations
    public void saveExam(Exam exam) {
        exams.put(exam.getExamId(), exam);
        saveExams();
    }

    public Exam getExam(String examId) {
        return exams.get(examId);
    }

    public Map<String, Exam> getAllExams() {
        return new HashMap<>(exams);
    }

    public List<Exam> getExamsByTeacher(String teacherId) {
        List<Exam> result = new ArrayList<>();
        for (Exam exam : exams.values()) {
            if (exam.getCreatedByTeacherId().equals(teacherId)) {
                result.add(exam);
            }
        }
        return result;
    }

    // Question operations
    public void saveQuestion(Question question) {
        questions.add(question);
        saveQuestions();
    }

    public List<Question> getQuestionsByExam(String examId) {
        List<Question> result = new ArrayList<>();
        for (Question q : questions) {
            if (q.getExamId().equals(examId)) {
                result.add(q);
            }
        }
        return result;
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    // Result operations
    public void saveResult(Result result) {
        results.add(result);
        saveResults();
    }

    public List<Result> getResultsByStudent(String studentId) {
        List<Result> result = new ArrayList<>();
        for (Result r : results) {
            if (r.getStudentId().equals(studentId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Result> getResultsByExam(String examId) {
        List<Result> result = new ArrayList<>();
        for (Result r : results) {
            if (r.getExamId().equals(examId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Result> getAllResults() {
        return new ArrayList<>(results);
    }
}
