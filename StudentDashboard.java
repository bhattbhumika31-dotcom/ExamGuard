package student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentDashboard extends JFrame {

    private JTable examTable;

    public StudentDashboard() {
        setTitle("ExamGuard - Student Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

       
        JPanel mainPanel = new JPanel(new BorderLayout());

      
        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(new Color(20, 30, 50));
        sidePanel.setPreferredSize(new Dimension(200, 600));
        sidePanel.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel title = new JLabel("ExamGuard");
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton examsBtn = new JButton("Exams");
        JButton resultsBtn = new JButton("Results");
        JButton logoutBtn = new JButton("Logout");

        sidePanel.add(title);
        sidePanel.add(examsBtn);
        sidePanel.add(resultsBtn);
        sidePanel.add(new JLabel()); 
        sidePanel.add(logoutBtn);

      
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(10, 20, 40));
        topBar.setPreferredSize(new Dimension(800, 50));

        JLabel heading = new JLabel("Available Exams");
        heading.setForeground(Color.WHITE);
        heading.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton newExamBtn = new JButton("Start New Exam");

        topBar.add(heading, BorderLayout.WEST);
        topBar.add(newExamBtn, BorderLayout.EAST);

     
        String[] columns = {"Exam ID", "Title", "Subject", "Duration", "Status"};

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        examTable = new JTable(model);

        JScrollPane tableScroll = new JScrollPane(examTable);

      
        JPanel bottomPanel = new JPanel();

        JButton startBtn = new JButton("Start");
        JButton resumeBtn = new JButton("Resume");
        JButton submitBtn = new JButton("Submit");
        JButton refreshBtn = new JButton("Refresh");

        bottomPanel.add(startBtn);
        bottomPanel.add(resumeBtn);
        bottomPanel.add(submitBtn);
        bottomPanel.add(refreshBtn);

       
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(topBar, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

      
        mainPanel.add(sidePanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentDashboard().setVisible(true);
        });
    }
}