package auth;

public class MenuController {

    private LoginService loginService;

    // Assume these classes already exist
    private TeacherMenu teacherMenu;
    private StudentMenu studentMenu;

    public MenuController() {
        loginService = new LoginService();

        // Initialize menus (already implemented elsewhere)
        teacherMenu = new TeacherMenu();
        studentMenu = new StudentMenu();
    }

    public void start() {
        String role = loginService.login();

        if (role.equals("TEACHER")) {
            teacherMenu.showMenu();
        } else if (role.equals("STUDENT")) {
            studentMenu.showMenu();
        }
    }
}