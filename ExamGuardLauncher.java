import auth.AuthenticationService;
import auth.AuthenticationDialog;
import auth.RoleSelectionDialog;
import auth.User;
import auth.UserRole;
import teacher.TeacherDashboard;

import javax.swing.*;

public class ExamGuardLauncher {

    private ExamGuardLauncher() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RoleSelectionDialog roleDialog = new RoleSelectionDialog(null);
            roleDialog.setVisible(true);

            UserRole selectedRole = roleDialog.getSelectedRole();
            if (selectedRole == null) {
                System.exit(0);
            }

            AuthenticationDialog authDialog = new AuthenticationDialog(null, selectedRole);
            authDialog.setVisible(true);

            User user = authDialog.getAuthenticatedUser();
            if (user != null) {
                if (user.getRole() == UserRole.TEACHER) {
                    TeacherDashboard teacherDashboard = new TeacherDashboard(user.getUserId(), user.getFullName());
                    teacherDashboard.setVisible(true);
                } else if (user.getRole() == UserRole.STUDENT) {
                    StudentDashboard studentDashboard = new StudentDashboard();
                    studentDashboard.setVisible(true);
                }
            }
        });
    }
}
