import core.DatabaseAuthenticator;
import teacher.TeacherDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AuthenticationWindow extends JFrame {

    private final JTextField idField;
    private final JLabel statusLabel;

    public AuthenticationWindow() {
        setTitle("ExamGuard Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        content.setBackground(Color.WHITE);

        JLabel header = new JLabel("ExamGuard Login");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel form = new JPanel(new BorderLayout(6, 6));
        form.setBackground(Color.WHITE);
        JLabel idLabel = new JLabel("Enter your ID:");
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        idField.setColumns(20);

        form.add(idLabel, BorderLayout.NORTH);
        form.add(idField, BorderLayout.CENTER);

        statusLabel = new JLabel("Type your student or teacher ID and click Login.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.DARK_GRAY);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.addActionListener(this::onLogin);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        exitButton.addActionListener(e -> System.exit(0));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(exitButton);
        buttons.add(loginButton);

        content.add(header, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    private void onLogin(ActionEvent event) {
        String enteredId = idField.getText();
        if (enteredId == null || enteredId.isBlank()) {
            statusLabel.setText("Please enter your ID before logging in.");
            return;
        }

        DatabaseAuthenticator.AuthResult authResult = DatabaseAuthenticator.findUserById(enteredId.trim());
        if (authResult == null) {
            statusLabel.setText("ID not found. Try again.");
            JOptionPane.showMessageDialog(this,
                    "No student or teacher record was found for ID: " + enteredId.trim(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        if (authResult.getRole() == DatabaseAuthenticator.UserRole.STUDENT) {
            StudentDashboard studentDashboard = new StudentDashboard(authResult.getId(), authResult.getName());
            studentDashboard.setVisible(true);
        } else {
            TeacherDashboard teacherDashboard = new TeacherDashboard(authResult.getId(), authResult.getName());
            teacherDashboard.setVisible(true);
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            AuthenticationWindow login = new AuthenticationWindow();
            login.setVisible(true);
        });
    }
}
