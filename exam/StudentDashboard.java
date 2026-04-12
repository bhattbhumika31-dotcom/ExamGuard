import core.DatabaseAuthenticator;
import model.Exam;
import model.Result;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class StudentDashboard extends JFrame {

    private static final int STATUS_COLUMN_INDEX = 7;

    private final StudentService studentService = new StudentService();
    private final DefaultTableModel tableModel;
    private final JTable table;

    private String studentId;
    private String studentName;
    private JLabel userLabel;

    public StudentDashboard() {
        setTitle("ExamGuard - Student Dashboard");
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(25, 30, 50));

        JPanel sidebar = buildSidebar();
        JPanel topBar = buildTopBar();

        String[] columns = {"Exam ID", "Title", "Subject", "Duration", "Questions", "Total Marks", "Teacher", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setBackground(new Color(30, 35, 55));
        table.setForeground(Color.WHITE);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(new Color(50, 55, 80));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(25, 30, 50));
        center.add(topBar, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buildActionBar(), BorderLayout.SOUTH);

        main.add(sidebar, BorderLayout.WEST);
        main.add(center, BorderLayout.CENTER);
        add(main);

        captureStudentProfile();
        if (studentId == null) {
            throw new IllegalStateException("Student access denied.");
        }
        refreshExamTable();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setBackground(new Color(15, 20, 35));
        sidebar.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel logo = new JLabel("ExamGuard", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton exams = new JButton("Available Exams");
        JButton results = new JButton("My Results");
        JButton refresh = new JButton("Refresh");

        styleSide(exams);
        styleSide(results);
        styleSide(refresh);

        exams.addActionListener(event -> refreshExamTable());
        results.addActionListener(event -> showResultsDialog());
        refresh.addActionListener(event -> refreshExamTable());

        sidebar.add(logo);
        sidebar.add(exams);
        sidebar.add(results);
        sidebar.add(refresh);
        sidebar.add(new JLabel());
        sidebar.add(new JLabel());
        return sidebar;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(1000, 90));
        top.setBackground(new Color(20, 25, 45));
        top.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Student Exam Center");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Timer starts with the exam, auto-submits on timeout, and locks after submission");
        subtitle.setForeground(Color.LIGHT_GRAY);

        titleBlock.add(title);
        titleBlock.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel role = new JLabel("STUDENT");
        role.setOpaque(true);
        role.setBackground(new Color(0, 150, 100));
        role.setForeground(Color.WHITE);
        role.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        userLabel = new JLabel();
        userLabel.setForeground(Color.WHITE);

        JButton logout = new JButton("Change Student");
        styleAction(logout, new Color(220, 53, 69));
        logout.addActionListener(event -> {
            captureStudentProfile();
            refreshExamTable();
        });

        right.add(role);
        right.add(userLabel);
        right.add(logout);

        top.add(titleBlock, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        bar.setBackground(new Color(20, 25, 45));

        JButton attempt = new JButton("Attempt Selected");
        JButton results = new JButton("View My Results");
        JButton refresh = new JButton("Refresh");

        styleAction(attempt, new Color(0, 123, 255));
        styleAction(results, new Color(255, 193, 7));
        styleAction(refresh, new Color(100, 100, 120));

        attempt.addActionListener(event -> attemptSelectedExam());
        results.addActionListener(event -> showResultsDialog());
        refresh.addActionListener(event -> refreshExamTable());

        bar.add(attempt);
        bar.add(results);
        bar.add(refresh);
        return bar;
    }

    private void captureStudentProfile() {
        while (true) {
            String enteredId = promptValue("Enter Student ID:", studentId == null ? "S001" : studentId);
            if (enteredId == null) {
                JOptionPane.showMessageDialog(this,
                    "Student access was cancelled.",
                    "Access Cancelled",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String enteredName = promptValue("Enter Student Name (optional):", studentName == null ? "" : studentName);
            if (enteredName == null) {
                enteredName = "";
            }

            List<Object> studentRecord = DatabaseAuthenticator.findStudent(enteredId, enteredName);
            if (studentRecord != null) {
                String resolvedStudentName = DatabaseAuthenticator.getColumnValue(studentRecord, "name");

                this.studentId = enteredId;
                this.studentName = resolvedStudentName != null && !resolvedStudentName.isBlank()
                    ? resolvedStudentName
                    : enteredName;
                userLabel.setText(studentName + " (" + studentId + ")");
                JOptionPane.showMessageDialog(this,
                    "Access approved. Welcome, " + studentName + "!",
                    "Student Verified",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this,
                "Access denied. Student record was not found in the database.",
                "Unauthorized Student",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String promptValue(String prompt, String initialValue) {
        String value = JOptionPane.showInputDialog(this, prompt, initialValue);
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            return initialValue;
        }
        return value.trim();
    }

    private void refreshExamTable() {
        tableModel.setRowCount(0);
        List<Exam> exams = studentService.getAvailableExams();
        for (Exam exam : exams) {
            tableModel.addRow(new Object[]{
                exam.getExamId(),
                exam.getTitle(),
                exam.getSubject(),
                exam.getDurationMinutes() + " min",
                exam.getQuestions().size(),
                exam.getTotalMarks(),
                exam.getCreatedByTeacherName(),
                studentService.getExamStatus(studentId, exam.getExamId())
            });
        }
    }

    private void attemptSelectedExam() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an exam first.");
            return;
        }

        String examId = tableModel.getValueAt(row, 0).toString();
        String examStatus = tableModel.getValueAt(row, STATUS_COLUMN_INDEX).toString();
        if ("Submitted".equalsIgnoreCase(examStatus) || studentService.hasSubmittedExam(studentId, examId)) {
            JOptionPane.showMessageDialog(this,
                "This exam has already been submitted. Re-attempts are not allowed.",
                "Attempt Locked",
                JOptionPane.INFORMATION_MESSAGE);
            refreshExamTable();
            return;
        }

        Result result = studentService.startExamWithDialog(this, studentId, studentName, examId);
        if (result != null) {
            refreshExamTable();
            showResultsDialog();
        }
    }

    private void showResultsDialog() {
        List<Result> results = studentService.getStudentResults(studentId);
        JTextArea area = new JTextArea(16, 54);
        area.setEditable(false);
        area.setBackground(new Color(25, 30, 50));
        area.setForeground(Color.WHITE);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        if (results.isEmpty()) {
            area.setText("No submitted results yet.");
        } else {
            StringBuilder builder = new StringBuilder();
            for (Result result : results) {
                builder.append(result.getExamId()).append(" - ").append(result.getExamTitle()).append('\n');
                builder.append("Score: ").append(result.getScore()).append("/").append(result.getTotalMarks())
                    .append(" | Percentage: ").append(String.format("%.1f%%", result.getPercentage()))
                    .append(" | Grade: ").append(result.getGrade()).append('\n');
                builder.append("Attempted: ").append(result.getAttemptDate()).append("\n\n");
            }
            area.setText(builder.toString());
        }

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "My Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleSide(JButton button) {
        button.setBackground(new Color(30, 35, 60));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void styleAction(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new StudentDashboard().setVisible(true);
            } catch (IllegalStateException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }
}
