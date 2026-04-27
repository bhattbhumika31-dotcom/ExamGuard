package core;

import model.Result;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseAuthenticator {

    private static final String URL = "jdbc:mysql://localhost:3306/examguard";
    private static final String USER = "root";
    private static final String PASSWORD = "mysql";

    private DatabaseAuthenticator() {
    }

    public static boolean isTeacherAuthorized(String teacherId, String teacherName) {
        return findTeacher(teacherId, teacherName) != null;
    }

    public static boolean isStudentAuthorized(String studentId, String studentName) {
        return findStudent(studentId, studentName) != null;
    }

    public static List<Object> findTeacher(String teacherId, String teacherName) {
        return findRecord("teachers", teacherId, teacherName);
    }

    public static List<Object> findStudent(String studentId, String studentName) {
        return findRecord("students", studentId, studentName);
    }

    public static AuthResult findUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }

        String trimmedId = userId.trim();
        List<Object> studentRecord = findStudent(trimmedId, "");
        if (studentRecord != null) {
            String resolvedName = getColumnValue(studentRecord, "name");
            return new AuthResult(UserRole.STUDENT, trimmedId,
                    resolvedName == null ? "" : resolvedName);
        }

        List<Object> teacherRecord = findTeacher(trimmedId, "");
        if (teacherRecord != null) {
            String resolvedName = getColumnValue(teacherRecord, "name");
            return new AuthResult(UserRole.TEACHER, trimmedId,
                    resolvedName == null ? "" : resolvedName);
        }

        return null;
    }

    public enum UserRole {
        STUDENT,
        TEACHER
    }

    public static final class AuthResult {
        private final UserRole role;
        private final String id;
        private final String name;

        public AuthResult(UserRole role, String id, String name) {
            this.role = role;
            this.id = id;
            this.name = name;
        }

        public UserRole getRole() {
            return role;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static String getColumnValue(List<Object> record, String columnName) {
        if (record == null || columnName == null || columnName.isBlank()) {
            return null;
        }

        String prefix = columnName.trim().toLowerCase() + "=";
        for (Object item : record) {
            if (item == null) {
                continue;
            }

            String text = String.valueOf(item);
            if (text.toLowerCase().startsWith(prefix)) {
                return text.substring(text.indexOf('=') + 1).trim();
            }
        }

        return null;
    }

    public static void saveStudentMark(Result result) {
        if (result == null) {
            return;
        }

        List<Object> studentRecord = findStudent(result.getStudentId(), result.getStudentName());
        if (studentRecord == null) {
            System.out.println("Unable to store marks because the student record was not found for ID: " + result.getStudentId());
            return;
        }

        String studentName = firstNonBlank(getColumnValue(studentRecord, "name"), result.getStudentName(), "Unknown");
        Integer studentId = parseIntegerValue(getColumnValue(studentRecord, "id"));
        Integer studentClass = parseIntegerValue(getColumnValue(studentRecord, "class"));

        if (studentId == null) {
            studentId = extractNumericId(result.getStudentId());
        }
        if (studentId == null) {
            System.out.println("Unable to store marks because a numeric student ID could not be resolved for: " + result.getStudentId());
            return;
        }
        if (studentClass == null) {
            studentClass = 0;
        }

        boolean passed = result.getPercentage() > 33.0;

        String sql = "INSERT INTO marks (name, `ID`, `class`, marks, status) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE name = VALUES(name), `class` = VALUES(`class`), marks = VALUES(marks), status = VALUES(status)";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, studentName);
                stmt.setInt(2, studentId);
                stmt.setInt(3, studentClass);
                stmt.setDouble(4, result.getScore());
                stmt.setBoolean(5, passed);
                stmt.executeUpdate();

                System.out.println("Stored marks in database for " + studentName
                        + " [ID=" + studentId + "]: " + result.getScore() + " (passed=" + passed + ")");
            }
        } catch (Exception e) {
            System.out.println("Failed to store marks for student ID '" + result.getStudentId() + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Object> findRecord(String table, String userId, String userName) {
        String safeTable = resolveTable(table);
        String trimmedId = userId == null ? "" : userId.trim();
        String trimmedName = userName == null ? "" : userName.trim();
        Integer numericId = extractNumericId(trimmedId);

        List<Object> parameters = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ")
            .append(safeTable)
            .append(" WHERE ");

        if (!trimmedId.isBlank()) {
            sql.append("CAST(id AS CHAR) = ?");
            parameters.add(trimmedId);

            if (numericId != null) {
                sql.append(" OR id = ?");
                parameters.add(numericId);
            }
        } else if (!trimmedName.isBlank()) {
            sql.append("LOWER(name) = LOWER(?)");
            parameters.add(trimmedName);
        } else {
            return null;
        }

        sql.append(" LIMIT 1");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

                for (int i = 0; i < parameters.size(); i++) {
                    Object value = parameters.get(i);
                    if (value instanceof Integer) {
                        stmt.setInt(i + 1, (Integer) value);
                    } else {
                        stmt.setString(i + 1, String.valueOf(value));
                    }
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        List<Object> data = new ArrayList<>();
                        ResultSetMetaData metaData = rs.getMetaData();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            data.add(metaData.getColumnLabel(i) + "=" + rs.getObject(i));
                        }
                        System.out.println("Database match from " + safeTable + ": " + data);
                        return data;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Database lookup failed for table '" + safeTable + "': " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("No matching record found in " + safeTable + " for id='" + trimmedId + "', name='" + trimmedName + "'.");
        return null;
    }

    private static String resolveTable(String table) {
        if ("teachers".equalsIgnoreCase(table)) {
            return "teachers";
        }
        if ("students".equalsIgnoreCase(table)) {
            return "students";
        }
        throw new IllegalArgumentException("Unsupported table: " + table);
    }

    private static Integer extractNumericId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String digits = value.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer parseIntegerValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return extractNumericId(value);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
