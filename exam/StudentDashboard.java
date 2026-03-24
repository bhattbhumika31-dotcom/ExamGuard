import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentDashboard extends JFrame {

    private JTable table;

    public StudentDashboard() {
        setTitle("ExamGuard - Student Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setBackground(new Color(15, 20, 35));
        sidebar.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel logo = new JLabel("ExamGuard", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);

        JButton exams = new JButton("Exams");
        JButton questions = new JButton("Questions");
        JButton analytics = new JButton("Analytics");

        styleSide(exams);
        styleSide(questions);
        styleSide(analytics);

        sidebar.add(logo);
        sidebar.add(exams);
        sidebar.add(questions);
        sidebar.add(analytics);
        sidebar.add(new JLabel());

        // ===== TOP BAR =====
        JPanel top = new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(1000, 60));
        top.setBackground(new Color(20, 25, 45));

        JLabel title = new JLabel("Exam Management");
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        JPanel right = new JPanel();
        right.setBackground(new Color(20, 25, 45));

        JLabel user = new JLabel("Bhumika Bhatt");
        user.setForeground(Color.WHITE);

        JButton role = new JButton("TEACHER");
        JButton logout = new JButton("Logout");

        styleRole(role);
        styleLogout(logout);

        right.add(role);
        right.add(user);
        right.add(logout);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        // ===== TABLE =====
        String[] cols = {"Exam ID", "Title", "Subject", "Duration (min)", "Questions", "Total Marks", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        table = new JTable(model);
        table.setBackground(new Color(30, 35, 55));
        table.setForeground(Color.WHITE);
        table.setRowHeight(30);

        table.getTableHeader().setBackground(new Color(50, 55, 80));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);

        // ===== BUTTON PANEL =====
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(20, 25, 45));

        JButton edit = new JButton("Edit");
        JButton toggle = new JButton("Toggle Status");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");

        styleAction(edit, new Color(255, 193, 7));
        styleAction(toggle, new Color(0, 123, 255));
        styleAction(delete, new Color(220, 53, 69));
        styleAction(refresh, new Color(100, 100, 120));

        bottom.add(edit);
        bottom.add(toggle);
        bottom.add(delete);
        bottom.add(refresh);

        // ===== CENTER =====
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(25, 30, 50));

        JLabel subTitle = new JLabel("Create, edit, and publish your exams");
        subTitle.setForeground(Color.LIGHT_GRAY);
        subTitle.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 30, 50));
        header.add(title, BorderLayout.NORTH);
        header.add(subTitle, BorderLayout.SOUTH);

        JButton newExam = new JButton("+ New Exam");
        styleAction(newExam, new Color(0, 123, 255));

        JPanel headerRight = new JPanel();
        headerRight.setBackground(new Color(25, 30, 50));
        headerRight.add(newExam);

        header.add(headerRight, BorderLayout.EAST);

        center.add(header, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        // ===== MAIN =====
        main.add(sidebar, BorderLayout.WEST);
        main.add(center, BorderLayout.CENTER);

        add(main);

        // sample data
        model.addRow(new Object[]{"1", "Midterm", "Math", "60", "20", "100", "Active"});
    }

    // ===== STYLING =====
    private void styleSide(JButton b) {
        b.setBackground(new Color(30, 35, 60));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    private void styleAction(JButton b, Color c) {
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    private void styleRole(JButton b) {
        b.setBackground(new Color(0, 150, 100));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    private void styleLogout(JButton b) {
        b.setBackground(Color.RED);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDashboard().setVisible(true));
    }
}//