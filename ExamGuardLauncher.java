import teacher.TeacherDashboard;

import javax.swing.SwingUtilities;

public final class ExamGuardLauncher {

    private ExamGuardLauncher() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TeacherDashboard.launch(null, null);

            try {
                StudentDashboard studentDashboard = new StudentDashboard();
                studentDashboard.setLocation(140, 90);
                studentDashboard.setVisible(true);
            } catch (IllegalStateException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }
}
