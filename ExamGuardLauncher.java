import teacher.TeacherDashboard;

import javax.swing.SwingUtilities;

public final class ExamGuardLauncher {

    private ExamGuardLauncher() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TeacherDashboard teacherDashboard = new TeacherDashboard("T001", "Bhumika Bhatt");
            teacherDashboard.setVisible(true);

            StudentDashboard studentDashboard = new StudentDashboard();
            studentDashboard.setLocation(teacherDashboard.getX() + 80, teacherDashboard.getY() + 60);
            studentDashboard.setVisible(true);
        });
    }
}
