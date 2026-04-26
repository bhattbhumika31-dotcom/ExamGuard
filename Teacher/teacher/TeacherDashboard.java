package teacher;

import core.ExamGuardRepository;
import model.Exam;
import model.Result;
import teacher.panels.AnalyticsPanel;
import teacher.panels.ExamPanel;
import teacher.panels.QuestionPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * ExamGuard – teacher/TeacherDashboard.java
 *
 * Main JFrame for the Teacher Module.
 * Called by the AUTH module after a teacher logs in.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────────────┐
 *   │  Top Bar  (Logo + Teacher name + Logout)             │
 *   ├──────────┬───────────────────────────────────────────┤
 *   │  Sidebar │  Content Area (JTabbedPane)               │
 *   │  • Exams │    Tab 1: ExamPanel                       │
 *   │  • Qns   │    Tab 2: QuestionPanel                   │
 *   │  • Stats │    Tab 3: AnalyticsPanel                  │
 *   └──────────┴───────────────────────────────────────────┘
 *
 * Integration hooks for Core module (Parth):
 *   dashboard.setExamStore(map)       – inject persistent exam store
 *   dashboard.setResultStore(list)    – inject persistent result list
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class TeacherDashboard extends JFrame {

    // ------------------------------------------------------------------ //
    //  Service layer
    // ------------------------------------------------------------------ //

    private final ExamManager     examManager;
    private final QuestionManager questionManager;
    private final ExamGuardRepository repository;

    // ------------------------------------------------------------------ //
    //  Panels
    // ------------------------------------------------------------------ //

    private ExamPanel      examPanel;
    private QuestionPanel  questionPanel;
    private AnalyticsPanel analyticsPanel;

    // ------------------------------------------------------------------ //
    //  Sidebar buttons (kept as fields for active-state tracking)
    // ------------------------------------------------------------------ //

    private JButton btnExams, btnQuestions, btnAnalytics;
    private JTabbedPane tabbedPane;

    // ------------------------------------------------------------------ //
    //  Session
    // ------------------------------------------------------------------ //

    private final String teacherId;
    private final String teacherName;

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public TeacherDashboard(String teacherId, String teacherName) {
        this.teacherId   = teacherId;
        this.teacherName = teacherName;
        this.repository  = ExamGuardRepository.getInstance();

        this.examManager     = new ExamManager();
        this.questionManager = new QuestionManager(examManager);
        this.examManager.setPersistenceHook(repository::persistExams);
        this.questionManager.setPersistenceHook(repository::persistExams);
        this.examManager.setExamStore(repository.getExamStore());

        initFrame();
        buildUI();
        setResultStore(repository.getResultStore());
    }

    // ------------------------------------------------------------------ //
    //  Core module hooks
    // ------------------------------------------------------------------ //

    /** Called by Core to inject the persistent exam store. */
    public void setExamStore(Map<String, Exam> store) {
        examManager.setExamStore(store);
        refreshAllPanels();
    }

    /** Called by Core to inject the persistent result list. */
    public void setResultStore(List<Result> results) {
        analyticsPanel.setResultStore(results);
    }

    // ------------------------------------------------------------------ //
    //  Frame init
    // ------------------------------------------------------------------ //

    private void initFrame() {
        setTitle("ExamGuard – Teacher Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        // Try system L&F for better native widgets, fall back to Nimbus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    // ------------------------------------------------------------------ //
    //  UI Build
    // ------------------------------------------------------------------ //

    private void buildUI() {
        setLayout(new BorderLayout());

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Top Bar ─────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_SIDEBAR);
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));
        bar.setPreferredSize(new Dimension(0, 58));

        // Logo
        JLabel logo = new JLabel("ExamGuard");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(UITheme.ACCENT_BLUE);
        bar.add(logo, BorderLayout.WEST);

        // Right side: role badge + name + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JLabel roleBadge = new JLabel("  TEACHER  ");
        roleBadge.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
        roleBadge.setForeground(UITheme.ACCENT_GREEN);
        roleBadge.setOpaque(true);
        roleBadge.setBackground(new Color(52, 199, 123, 30));
        roleBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_GREEN, 1),
                new EmptyBorder(2, 6, 2, 6)));

        JLabel nameLabel = UITheme.makeLabel(teacherName, UITheme.FONT_BODY, UITheme.TEXT_PRIMARY);

        JButton logoutBtn = UITheme.makeSmallButton("Logout", UITheme.ACCENT_RED);
        logoutBtn.setPreferredSize(new Dimension(80, 28));
        logoutBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Log out from ExamGuard?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                dispose();
                // AUTH module re-opens on logout — hook here when integrating
            }
        });

        right.add(roleBadge);
        right.add(nameLabel);
        right.add(logoutBtn);
        bar.add(right, BorderLayout.EAST);

        // Separator line at bottom
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(0, 20, 0, 20)));

        return bar;
    }

    // ── Sidebar ─────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR),
                new EmptyBorder(20, 0, 20, 0)));

        // Section header
        JLabel nav = UITheme.makeMuted("  NAVIGATION");
        nav.setBorder(new EmptyBorder(0, 16, 10, 0));
        sidebar.add(nav);

        btnExams     = makeSidebarButton("Exams",      0);
        btnQuestions = makeSidebarButton("Questions",  1);
        btnAnalytics = makeSidebarButton("Analytics",  2);

        sidebar.add(btnExams);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnQuestions);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnAnalytics);
        sidebar.add(Box.createVerticalGlue());

        // Version footer
        JLabel ver = UITheme.makeMuted("  ExamGuard v1.0");
        ver.setBorder(new EmptyBorder(0, 16, 0, 0));
        sidebar.add(ver);

        return sidebar;
    }

    private JButton makeSidebarButton(String text, int tabIndex) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (tabbedPane != null && tabbedPane.getSelectedIndex() == tabIndex) {
                    g2.setColor(new Color(66, 133, 244, 30));
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);
                    g2.setColor(UITheme.ACCENT_BLUE);
                    g2.fillRoundRect(0, (getHeight() - 28) / 2, 4, 28, 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(200, 42));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 18, 0, 0));
        btn.addActionListener(e -> {
            tabbedPane.setSelectedIndex(tabIndex);
            repaint();
        });
        return btn;
    }

    // ── Content Area ────────────────────────────────────────────────────

    private JPanel buildContent() {
        examPanel      = new ExamPanel(examManager, teacherId, teacherName);
        questionPanel  = new QuestionPanel(examManager, questionManager, teacherId);
        analyticsPanel = new AnalyticsPanel(examManager, teacherId);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UITheme.BG_DARK);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);
        // Hide default tab bar – navigation is via sidebar
        tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        tabbedPane.addTab("Exams",     examPanel);
        tabbedPane.addTab("Questions", questionPanel);
        tabbedPane.addTab("Analytics", analyticsPanel);

        // Make tab bar invisible (sidebar drives navigation)
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected int calculateTabAreaHeight(int placement, int horz, int maxTabH) { return 0; }
            @Override protected void paintTabArea(Graphics g, int tp, int si) {}
        });

        // When tab changes, refresh question combo & analytics combo
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 1) questionPanel.populateExamCombo();
            if (idx == 2) analyticsPanel.populateCombo();
            repaintSidebar();
        });

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.BG_DARK);
        content.add(tabbedPane, BorderLayout.CENTER);
        return content;
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void repaintSidebar() {
        btnExams.repaint();
        btnQuestions.repaint();
        btnAnalytics.repaint();
    }

    private void refreshAllPanels() {
        if (examPanel != null)     examPanel.refreshTable();
        if (questionPanel != null) questionPanel.populateExamCombo();
        if (analyticsPanel != null) analyticsPanel.populateCombo();
    }

    // ------------------------------------------------------------------ //
    //  Entry point (standalone test)
    // ------------------------------------------------------------------ //

    /**
     * Launches the Teacher Dashboard.
     * In the full system this is called by the AUTH module.
     */
    public static void launch(String teacherId, String teacherName) {
        SwingUtilities.invokeLater(() -> {
            TeacherDashboard dashboard = new TeacherDashboard(teacherId, teacherName);
            dashboard.setVisible(true);
        });
    }

    /** Quick standalone test — remove when integrating with AUTH. */
    public static void main(String[] args) {
        // Seed some demo data so the dashboard looks populated on first run
        TeacherDashboard.launch("T001", "Bhumika Bhatt");
    }
}
