package auth;

import javax.swing.*;
import java.awt.*;

public class RoleSelectionDialog extends JDialog {
    private UserRole selectedRole;

    public RoleSelectionDialog(Frame parent) {
        super(parent, "ExamGuard - Select Role", true);
        initializeUI();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 350);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(AppTheme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Welcome to ExamGuard");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Select your role to continue");
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);

        // Teacher Button
        JButton teacherBtn = new JButton("Teacher");
        teacherBtn.setFont(AppTheme.FONT_HEADER);
        teacherBtn.setBackground(AppTheme.PRIMARY_BLUE);
        teacherBtn.setForeground(AppTheme.TEXT_PRIMARY);
        teacherBtn.setPreferredSize(new Dimension(180, 60));
        teacherBtn.setFocusPainted(false);
        teacherBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        teacherBtn.addActionListener(e -> {
            selectedRole = UserRole.TEACHER;
            dispose();
        });

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(teacherBtn, gbc);

        // Student Button
        JButton studentBtn = new JButton("Student");
        studentBtn.setFont(AppTheme.FONT_HEADER);
        studentBtn.setBackground(AppTheme.ACCENT_GREEN);
        studentBtn.setForeground(AppTheme.TEXT_PRIMARY);
        studentBtn.setPreferredSize(new Dimension(180, 60));
        studentBtn.setFocusPainted(false);
        studentBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        studentBtn.addActionListener(e -> {
            selectedRole = UserRole.STUDENT;
            dispose();
        });

        gbc.gridx = 1;
        mainPanel.add(studentBtn, gbc);

        // Exit Button
        JButton exitBtn = new JButton("Exit");
        exitBtn.setFont(AppTheme.FONT_BODY);
        exitBtn.setBackground(AppTheme.ACCENT_RED);
        exitBtn.setForeground(AppTheme.TEXT_PRIMARY);
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> System.exit(0));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(exitBtn, gbc);

        add(mainPanel);
    }

    public UserRole getSelectedRole() {
        return selectedRole;
    }
}
