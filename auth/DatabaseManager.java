package auth;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:examguard.db";

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    private void createTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id TEXT PRIMARY KEY," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "full_name TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "is_active BOOLEAN DEFAULT 1," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            insertDefaultUsers();
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    private void insertDefaultUsers() {
        String checkQuery = "SELECT COUNT(*) FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default users only if table is empty
                String insertQuery = "INSERT INTO users (user_id, username, password, full_name, role) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                    // Teachers
                    pstmt.setString(1, "T001");
                    pstmt.setString(2, "teacher1");
                    pstmt.setString(3, "pass123");
                    pstmt.setString(4, "Bhumika Bhatt");
                    pstmt.setString(5, "TEACHER");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "T002");
                    pstmt.setString(2, "teacher2");
                    pstmt.setString(3, "pass123");
                    pstmt.setString(4, "John Smith");
                    pstmt.setString(5, "TEACHER");
                    pstmt.executeUpdate();

                    // Students
                    pstmt.setString(1, "S001");
                    pstmt.setString(2, "student1");
                    pstmt.setString(3, "pass123");
                    pstmt.setString(4, "Alice Johnson");
                    pstmt.setString(5, "STUDENT");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "S002");
                    pstmt.setString(2, "student2");
                    pstmt.setString(3, "pass123");
                    pstmt.setString(4, "Bob Wilson");
                    pstmt.setString(5, "STUDENT");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "S003");
                    pstmt.setString(2, "student3");
                    pstmt.setString(3, "pass123");
                    pstmt.setString(4, "Carol Davis");
                    pstmt.setString(5, "STUDENT");
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting default users: " + e.getMessage());
        }
    }

    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("user_id");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");
                return new User(userId, username, password, fullName, UserRole.fromString(role));
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("user_id");
                String password = rs.getString("password");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");
                return new User(userId, username, password, fullName, UserRole.fromString(role));
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    public boolean registerUser(User user) {
        String query = "INSERT INTO users (user_id, username, password, full_name, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole().getValue());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
