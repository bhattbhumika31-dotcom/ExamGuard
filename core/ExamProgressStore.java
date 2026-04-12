package core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExamProgressStore {

    private static final Path PROGRESS_DIR = Paths.get("data", "progress");

    private ExamProgressStore() {
    }

    public static void saveProgress(String studentId, String studentName, String examId, String examTitle,
                                    int currentQuestionIndex, long startedAtMillis, Map<Integer, String> answers) {
        if (studentId == null || studentId.isBlank() || examId == null || examId.isBlank()) {
            return;
        }

        Map<Integer, String> safeAnswers = answers == null ? Collections.emptyMap() : new LinkedHashMap<>(answers);
        String json = toJson(studentId, studentName, examId, examTitle, currentQuestionIndex, startedAtMillis, safeAnswers);

        try {
            Files.createDirectories(PROGRESS_DIR);
            Files.writeString(getProgressFile(studentId, examId), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.out.println("Unable to save exam progress: " + ex.getMessage());
        }
    }

    public static SavedProgress loadProgress(String studentId, String examId) {
        Path file = getProgressFile(studentId, examId);
        if (!Files.exists(file)) {
            return null;
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            return fromJson(json);
        } catch (IOException ex) {
            System.out.println("Unable to load saved exam progress: " + ex.getMessage());
            return null;
        }
    }

    public static void clearProgress(String studentId, String examId) {
        Path file = getProgressFile(studentId, examId);
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            System.out.println("Unable to clear saved exam progress: " + ex.getMessage());
        }
    }

    private static Path getProgressFile(String studentId, String examId) {
        return PROGRESS_DIR.resolve(sanitize(studentId) + "_" + sanitize(examId) + ".json");
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String toJson(String studentId, String studentName, String examId, String examTitle,
                                 int currentQuestionIndex, long startedAtMillis, Map<Integer, String> answers) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"studentId\": \"").append(escape(studentId)).append("\",\n");
        builder.append("  \"studentName\": \"").append(escape(studentName)).append("\",\n");
        builder.append("  \"examId\": \"").append(escape(examId)).append("\",\n");
        builder.append("  \"examTitle\": \"").append(escape(examTitle)).append("\",\n");
        builder.append("  \"currentQuestionIndex\": ").append(Math.max(currentQuestionIndex, 0)).append(",\n");
        builder.append("  \"startedAtMillis\": ").append(Math.max(startedAtMillis, 0L)).append(",\n");
        builder.append("  \"answers\": {");

        int count = 0;
        for (Map.Entry<Integer, String> entry : answers.entrySet()) {
            if (count == 0) {
                builder.append("\n");
            } else {
                builder.append(",\n");
            }
            builder.append("    \"").append(entry.getKey()).append("\": \"")
                .append(escape(entry.getValue())).append("\"");
            count++;
        }

        if (!answers.isEmpty()) {
            builder.append("\n");
        }
        builder.append("  }\n");
        builder.append("}\n");
        return builder.toString();
    }

    private static SavedProgress fromJson(String json) {
        String studentId = extractString(json, "studentId");
        String studentName = extractString(json, "studentName");
        String examId = extractString(json, "examId");
        String examTitle = extractString(json, "examTitle");
        int currentQuestionIndex = extractInt(json, "currentQuestionIndex", 0);
        long startedAtMillis = extractLong(json, "startedAtMillis", 0L);
        Map<Integer, String> answers = extractAnswers(json);

        return new SavedProgress(studentId, studentName, examId, examTitle, currentQuestionIndex, startedAtMillis, answers);
    }

    private static String extractString(String json, String field) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescape(matcher.group(1));
        }
        return "";
    }

    private static int extractInt(String json, String field, int defaultValue) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static long extractLong(String json, String field, long defaultValue) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static Map<Integer, String> extractAnswers(String json) {
        Map<Integer, String> answers = new LinkedHashMap<>();
        Pattern blockPattern = Pattern.compile("\\\"answers\\\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher blockMatcher = blockPattern.matcher(json);
        if (!blockMatcher.find()) {
            return answers;
        }

        Pattern pairPattern = Pattern.compile("\\\"(\\d+)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher pairMatcher = pairPattern.matcher(blockMatcher.group(1));
        while (pairMatcher.find()) {
            answers.put(Integer.parseInt(pairMatcher.group(1)), unescape(pairMatcher.group(2)));
        }
        return answers;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static String unescape(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }

    public static final class SavedProgress {
        private final String studentId;
        private final String studentName;
        private final String examId;
        private final String examTitle;
        private final int currentQuestionIndex;
        private final long startedAtMillis;
        private final Map<Integer, String> answers;

        private SavedProgress(String studentId, String studentName, String examId, String examTitle,
                              int currentQuestionIndex, long startedAtMillis, Map<Integer, String> answers) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.examId = examId;
            this.examTitle = examTitle;
            this.currentQuestionIndex = currentQuestionIndex;
            this.startedAtMillis = startedAtMillis;
            this.answers = new LinkedHashMap<>(answers);
        }

        public String getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getExamId() {
            return examId;
        }

        public String getExamTitle() {
            return examTitle;
        }

        public int getCurrentQuestionIndex() {
            return currentQuestionIndex;
        }

        public long getStartedAtMillis() {
            return startedAtMillis;
        }

        public Map<Integer, String> getAnswers() {
            return new LinkedHashMap<>(answers);
        }
    }
}
