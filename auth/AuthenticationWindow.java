package auth;

import core.DatabaseAuthenticator;
import exam.StudentDashboard;
import teacher.TeacherDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AuthenticationWindow extends JFrame {

    private final JTextField idField;
    private final JPasswordField passwordField;
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

        JPanel form = new JPanel(new GridLayout(4, 1, 6, 6));
        form.setBackground(Color.WHITE);

        JLabel idLabel = new JLabel("Enter your ID:");
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel passwordLabel = new JLabel("Enter your password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        form.add(idLabel);
        form.add(idField);
        form.add(passwordLabel);
        form.add(passwordField);

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
        String enteredPassword = String.valueOf(passwordField.getPassword());
        if (enteredId == null || enteredId.isBlank()) {
            statusLabel.setText("Please enter your ID before logging in.");
            return;
        }
        if (enteredPassword == null || enteredPassword.isBlank()) {
            statusLabel.setText("Please enter your password.");
            return;
        }

        DatabaseAuthenticator.AuthResult authResult = DatabaseAuthenticator.authenticateUser(enteredId.trim(), enteredPassword);
        if (authResult == null) {
            statusLabel.setText("Invalid ID or password. Try again.");
            JOptionPane.showMessageDialog(this,
                    "ID and password mismatch. Please check your credentials.",
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
