package auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AuthenticationDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JButton submitButton;
    private JButton toggleButton;
    private User authenticatedUser;
    private boolean isLoginMode = true;
    private UserRole userRole;

    public AuthenticationDialog(Frame parent, UserRole role) {
        super(parent, "ExamGuard - " + role.getValue(), true);
        this.userRole = role;
        initializeUI();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(450, 450);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(AppTheme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("ExamGuard - " + userRole.getValue());
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Mode Label
        JLabel modeLabel = new JLabel("Login");
        modeLabel.setFont(AppTheme.FONT_SMALL);
        modeLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridy = 1;
        mainPanel.add(modeLabel, gbc);

        // Full Name (Signup only)
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        fullNameLabel.setVisible(false);
        mainPanel.add(fullNameLabel, gbc);
        fullNameField = new JTextField(15);
        fullNameField.setBackground(AppTheme.BG_CARD);
        fullNameField.setForeground(AppTheme.TEXT_PRIMARY);
        fullNameField.setCaretColor(AppTheme.TEXT_PRIMARY);
        fullNameField.setVisible(false);
        gbc.gridx = 1;
        mainPanel.add(fullNameField, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        mainPanel.add(usernameLabel, gbc);
        usernameField = new JTextField(15);
        usernameField.setBackground(AppTheme.BG_CARD);
        usernameField.setForeground(AppTheme.TEXT_PRIMARY);
        usernameField.setCaretColor(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(AppTheme.TEXT_PRIMARY);
        mainPanel.add(passwordLabel, gbc);
        passwordField = new JPasswordField(15);
        passwordField.setBackground(AppTheme.BG_CARD);
        passwordField.setForeground(AppTheme.TEXT_PRIMARY);
        passwordField.setCaretColor(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // Confirm Password (Signup only)
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setForeground(AppTheme.TEXT_PRIMARY);
        confirmLabel.setVisible(false);
        mainPanel.add(confirmLabel, gbc);
        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setBackground(AppTheme.BG_CARD);
        confirmPasswordField.setForeground(AppTheme.TEXT_PRIMARY);
        confirmPasswordField.setCaretColor(AppTheme.TEXT_PRIMARY);
        confirmPasswordField.setVisible(false);
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        // Info
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("Create account or use existing credentials");
        infoLabel.setFont(AppTheme.FONT_SMALL);
        infoLabel.setForeground(AppTheme.TEXT_SECONDARY);
        mainPanel.add(infoLabel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(AppTheme.BG_DARK);
        
        submitButton = new JButton("Login");
        submitButton.setBackground(AppTheme.PRIMARY_BLUE);
        submitButton.setForeground(AppTheme.TEXT_PRIMARY);
        submitButton.setFont(AppTheme.FONT_BODY);
        submitButton.setFocusPainted(false);
        
        toggleButton = new JButton("Create Account");
        toggleButton.setBackground(AppTheme.ACCENT_GREEN);
        toggleButton.setForeground(AppTheme.TEXT_PRIMARY);
        toggleButton.setFont(AppTheme.FONT_BODY);
        toggleButton.setFocusPainted(false);
        
        JButton exitBtn = new JButton("Exit");
        exitBtn.setBackground(AppTheme.ACCENT_RED);
        exitBtn.setForeground(AppTheme.TEXT_PRIMARY);
        exitBtn.setFont(AppTheme.FONT_BODY);
        exitBtn.setFocusPainted(false);

        submitButton.addActionListener(this::handleSubmit);
        toggleButton.addActionListener(e -> toggleMode(modeLabel, fullNameLabel, confirmLabel));
        exitBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(submitButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(exitBtn);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
    }

    private void toggleMode(JLabel modeLabel, JLabel fullNameLabel, JLabel confirmLabel) {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            submitButton.setText("Login");
            toggleButton.setText("Create Account");
            modeLabel.setText("Login");
            fullNameField.setVisible(false);
            fullNameLabel.setVisible(false);
            confirmPasswordField.setVisible(false);
            confirmLabel.setVisible(false);
        } else {
            submitButton.setText("Sign Up");
            toggleButton.setText("Back to Login");
            modeLabel.setText("Sign Up");
            fullNameField.setVisible(true);
            fullNameLabel.setVisible(true);
            confirmPasswordField.setVisible(true);
            confirmLabel.setVisible(true);
            fullNameField.setText("");
        }

        revalidate();
        repaint();
    }

    private void handleSubmit(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AuthenticationService authService = AuthenticationService.getInstance();

        if (isLoginMode) {
            handleLogin(authService, username, password);
        } else {
            handleSignup(authService, username, password);
        }
    }

    private void handleLogin(AuthenticationService authService, String username, String password) {
        User user = authService.authenticate(username, password);

        if (user != null && user.getRole() == userRole) {
            this.authenticatedUser = user;
            dispose();
        } else if (user != null) {
            JOptionPane.showMessageDialog(this, "This account is for " + user.getRole().getValue(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void handleSignup(AuthenticationService authService, String username, String password) {
        String fullName = fullNameField.getText().trim();
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter full name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            confirmPasswordField.setText("");
            passwordField.setText("");
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (authService.getUser(username) != null) {
            JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
            return;
        }

        // Create new user
        String userId = userRole == UserRole.TEACHER ? "T" : "S";
        userId += System.currentTimeMillis() % 10000;

        User newUser = new User(userId, username, password, fullName, userRole);
        authService.registerUser(newUser);
        authService.saveUsers();

        JOptionPane.showMessageDialog(this, "Account created successfully! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);

        // Switch to login mode
        isLoginMode = true;
        submitButton.setText("Login");
        toggleButton.setText("Create Account");
        fullNameField.setVisible(false);
        confirmPasswordField.setVisible(false);
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        
        revalidate();
        repaint();
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
