import auth.AuthenticationWindow;
import javax.swing.SwingUtilities;

public final class ExamGuardLauncher {

    private ExamGuardLauncher() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthenticationWindow::launch);
    }
}
