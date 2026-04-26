package auth;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SimpleUserDatabase {
    private static SimpleUserDatabase instance;
    private Map<String, User> users;
    private static final String DB_FILE = "users.db";

    private SimpleUserDatabase() {
        users = new HashMap<>();
        loadUsers();
    }

    public static synchronized SimpleUserDatabase getInstance() {
        if (instance == null) {
            instance = new SimpleUserDatabase();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(DB_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (Map<String, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading users: " + e.getMessage());
                initializeDefaultUsers();
            }
        } else {
            initializeDefaultUsers();
        }
    }

    private void initializeDefaultUsers() {
        users.put("teacher1", new User("T001", "teacher1", "pass123", "Bhumika Bhatt", UserRole.TEACHER));
        users.put("teacher2", new User("T002", "teacher2", "pass123", "John Smith", UserRole.TEACHER));
        users.put("student1", new User("S001", "student1", "pass123", "Alice Johnson", UserRole.STUDENT));
        users.put("student2", new User("S002", "student2", "pass123", "Bob Wilson", UserRole.STUDENT));
        users.put("student3", new User("S003", "student3", "pass123", "Carol Davis", UserRole.STUDENT));
        saveUsers();
    }

    public void saveUsers() {
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DB_FILE))) {
                oos.writeObject(users);
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public User authenticateUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.isActive() && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User getUserByUsername(String username) {
        return users.get(username);
    }

    public boolean registerUser(User user) {
        if (!users.containsKey(user.getUsername())) {
            users.put(user.getUsername(), user);
            saveUsers();
            return true;
        }
        return false;
    }
}
