import java.util.HashMap;
import java.util.Scanner;

public class AuthenticationSystem {

    private HashMap<String, User> users = new HashMap<>();
    private final int MAX_ATTEMPTS = 3;

    public AuthenticationSystem() {
        // Predefined users (for testing)
        users.put("student1", new User("student1", "1234", "student"));
        users.put("admin1", new User("admin1", "admin123", "admin"));
    }

    public void login() {
        Scanner sc = new Scanner(System.in);
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {

            System.out.print("Enter Username: ");
            String username = sc.nextLine();

            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            if (validateUser(username, password)) {
                User loggedInUser = users.get(username);
                System.out.println("\nLogin Successful!");
                System.out.println("Welcome " + loggedInUser.getRole().toUpperCase());
                return;
            } else {
                attempts++;
                System.out.println("Invalid credentials! Attempts left: " 
                                    + (MAX_ATTEMPTS - attempts));
            }
        }

        System.out.println("\nToo many failed attempts. Account locked!");
    }

    private boolean validateUser(String username, String password) {

        if (users.containsKey(username)) {
            User user = users.get(username);
            return user.getPassword().equals(password);
        }
        return false;
    }

    public static void main(String[] args) {

        AuthenticationSystem auth = new AuthenticationSystem();
        auth.login();
    }
}