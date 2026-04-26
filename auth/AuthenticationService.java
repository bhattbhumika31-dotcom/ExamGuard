package auth;

public class AuthenticationService {
    private static AuthenticationService instance;
    private User currentUser;
    private SimpleUserDatabase userDb;

    private AuthenticationService() {
        userDb = SimpleUserDatabase.getInstance();
    }

    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    public User authenticate(String username, String password) {
        User user = userDb.authenticateUser(username, password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public void registerUser(User user) {
        userDb.registerUser(user);
    }

    public User getUser(String username) {
        return userDb.getUserByUsername(username);
    }

    public void saveUsers() {
        userDb.saveUsers();
    }
}
