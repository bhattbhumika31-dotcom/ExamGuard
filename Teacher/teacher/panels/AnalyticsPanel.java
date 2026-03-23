package teacher.panels;

import model.Exam;
import model.Result;
import teacher.ExamManager;
import teacher.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExamGuard – teacher/panels/AnalyticsPanel.java
 *
 * JPanel for the "Analytics & Records" tab in the Teacher Dashboard.
 *
 * Features:
 *   • Exam Overview – summary stats card per exam
 *   • Result Roster – ranked table of students for a selected exam
 *   • Grade Distribution – visual bar chart
 *   • Student search
 *
 * The resultStore is injected by TeacherDashboard after the Core module
 * provides it; by default an empty list is used.
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class AnalyticsPanel extends JPanel {

    // ------------------------------------------------------------------ //
    //  Fields
    // ------------------------------------------------------------------ //

    private final ExamManager   examManager;
    private final String        teacherId;
    private List<Result>    resultStore = new ArrayList<>();

    private JComboBox<String>  examCombo;
    private JTable             rosterTable;
    private DefaultTableModel  rosterModel;
    private JLabel             statAttempts, statAvg, statPass, statFail, statHigh, statLow;
    private JPanel             gradeBarPanel;
    private JTextField         searchField;

    private static final String[] ROSTER_COLS =
            {"Rank", "Student Name", "Score", "Percentage", "Grade", "Status", "Date"};

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public AnalyticsPanel(ExamManager examManager, String teacherId) {
        this.examManager = examManager;
        this.teacherId   = teacherId;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 8));
        buildUI();
    }

    /** Called by TeacherDashboard when the Core module provides the result list. */
    public void setResultStore(List<Result> resultStore) {
        this.resultStore = resultStore;
        refresh();
    }

    // ------------------------------------------------------------------ //
    //  UI Construction
    // ------------------------------------------------------------------ //

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(new EmptyBorder(20, 24, 8, 24));
        header.add(UITheme.makeTitle("Analytics & Records"), BorderLayout.WEST);

        // Exam selector + search row
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controls.setBackground(UITheme.BG_DARK);
        controls.setBorder(new EmptyBorder(0, 24, 10, 24));

        controls.add(UITheme.makeLabel("Exam:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));
        examCombo = new JComboBox<>();
        examCombo.setBackground(UITheme.BG_DARK);
        examCombo.setForeground(UITheme.TEXT_PRIMARY);
        examCombo.setFont(UITheme.FONT_BODY);
        examCombo.setPreferredSize(new Dimension(260, 30));
        examCombo.addActionListener(e -> refresh());
        controls.add(examCombo);

        JButton loadBtn = UITheme.makeSmallButton("↻ Load", new Color(70, 80, 110));
        loadBtn.addActionListener(e -> { populateCombo(); refresh(); });
        controls.add(loadBtn);

        controls.add(Box.createHorizontalStrut(20));
        controls.add(UITheme.makeLabel("Search:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));
        searchField = UITheme.makeTextField(14);
        searchField.setPreferredSize(new Dimension(160, 30));
        searchField.addActionListener(e -> applySearch());
        controls.add(searchField);
        JButton searchBtn = UITheme.makeSmallButton("Go", UITheme.ACCENT_BLUE);
        searchBtn.addActionListener(e -> applySearch());
        controls.add(searchBtn);

        JPanel topBlock = new JPanel(new BorderLayout());
        topBlock.setBackground(UITheme.BG_DARK);
        topBlock.add(header,   BorderLayout.NORTH);
        topBlock.add(controls, BorderLayout.SOUTH);
        add(topBlock, BorderLayout.NORTH);

        // ── Centre split: stats + roster + grade chart ───────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPane(), buildRightPane());
        split.setDividerLocation(280);
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        split.setBackground(UITheme.BG_DARK);
        split.setBorder(new EmptyBorder(0, 24, 16, 24));
        add(split, BorderLayout.CENTER);

        populateCombo();
    }

    // ── Left pane: Stats cards + Grade distribution ──────────────────────
    private JPanel buildLeftPane() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UITheme.BG_DARK);

        left.add(buildStatsCard());
        left.add(Box.createVerticalStrut(12));
        left.add(buildGradeCard());
        return left;
    }

    private JPanel buildStatsCard() {
        JPanel card = UITheme.makeCard();
        card.setLayout(new GridLayout(0, 1, 0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        card.add(UITheme.makeSubtitle("Exam Statistics"));

        statAttempts = makeStat("Attempts",  "–");
        statAvg      = makeStat("Average",   "–");
        statHigh     = makeStat("Highest",   "–");
        statLow      = makeStat("Lowest",    "–");
        statPass     = makeStat("Passed",    "–");
        statFail     = makeStat("Failed",    "–");

        for (JLabel l : new JLabel[]{statAttempts, statAvg, statHigh, statLow, statPass, statFail}) {
            card.add(l);
        }
        return card;
    }

    /** Creates a "Key : Value" label line. */
    private JLabel makeStat(String key, String val) {
        JLabel lbl = new JLabel("<html><span style='color:#9AA0AF'>" + key
                + ": </span><b style='color:#E8EAF0'>" + val + "</b></html>");
        lbl.setFont(UITheme.FONT_BODY);
        return lbl;
    }

    private void setStat(JLabel lbl, String key, String val) {
        lbl.setText("<html><span style='color:#9AA0AF'>" + key
                + ": </span><b style='color:#E8EAF0'>" + val + "</b></html>");
    }

    private JPanel buildGradeCard() {
        JPanel card = UITheme.makeCard();
        card.setLayout(new BorderLayout(0, 8));
        card.add(UITheme.makeSubtitle("Grade Distribution"), BorderLayout.NORTH);

        gradeBarPanel = new JPanel();
        gradeBarPanel.setLayout(new BoxLayout(gradeBarPanel, BoxLayout.Y_AXIS));
        gradeBarPanel.setBackground(UITheme.BG_PANEL);
        card.add(gradeBarPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Right pane: Result Roster table ─────────────────────────────────
    private JPanel buildRightPane() {
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(UITheme.BG_DARK);

        right.add(UITheme.makeSubtitle("Result Roster"), BorderLayout.NORTH);

        rosterModel = new DefaultTableModel(ROSTER_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rosterTable = new JTable(rosterModel);
        UITheme.styleTable(rosterTable);

        // Colour Status column
        rosterTable.getColumnModel().getColumn(5).setCellRenderer(
                (tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(val == null ? "" : val.toString(), SwingConstants.CENTER);
            boolean pass = "PASS".equals(val);
            lbl.setForeground(pass ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
            lbl.setFont(UITheme.FONT_BTN);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.ACCENT_BLUE.darker()
                    : (row % 2 == 0 ? UITheme.TABLE_ROW_EVEN : UITheme.TABLE_ROW_ODD));
            return lbl;
        });

        JScrollPane scroll = UITheme.makeScrollPane(rosterTable);
        right.add(scroll, BorderLayout.CENTER);
        return right;
    }

    // ------------------------------------------------------------------ //
    //  Data Refresh
    // ------------------------------------------------------------------ //

    public void populateCombo() {
        examCombo.removeAllItems();
        examManager.getExamsByTeacher(teacherId)
                   .forEach(e -> examCombo.addItem(e.getExamId() + " – " + e.getTitle()));
        refresh();
    }

    private void refresh() {
        String selectedExam = (String) examCombo.getSelectedItem();
        if (selectedExam == null) { clearAll(); return; }
        String examId = selectedExam.split("–")[0].trim();

        List<Result> rs = resultStore.stream()
                .filter(r -> r.getExamId().equals(examId))
                .collect(Collectors.toList());

        updateStatsCard(rs);
        updateRoster(rs, "");
        updateGradeChart(rs);
    }

    private void applySearch() {
        String selectedExam = (String) examCombo.getSelectedItem();
        if (selectedExam == null) return;
        String examId = selectedExam.split("–")[0].trim();
        String query  = searchField.getText().trim().toLowerCase();

        List<Result> rs = resultStore.stream()
                .filter(r -> r.getExamId().equals(examId))
                .filter(r -> query.isEmpty() || r.getStudentName().toLowerCase().contains(query))
                .collect(Collectors.toList());

        updateRoster(rs, query);
    }

    private void updateStatsCard(List<Result> rs) {
        if (rs.isEmpty()) {
            setStat(statAttempts, "Attempts", "0");
            setStat(statAvg,  "Average", "–");
            setStat(statHigh, "Highest", "–");
            setStat(statLow,  "Lowest",  "–");
            setStat(statPass, "Passed",  "0");
            setStat(statFail, "Failed",  "0");
            return;
        }
        DoubleSummaryStatistics stats = rs.stream()
                .mapToDouble(Result::getPercentage).summaryStatistics();
        long passed = rs.stream().filter(Result::isPassed).count();

        setStat(statAttempts, "Attempts", String.valueOf(rs.size()));
        setStat(statAvg,  "Average", String.format("%.1f%%", stats.getAverage()));
        setStat(statHigh, "Highest", String.format("%.1f%%", stats.getMax()));
        setStat(statLow,  "Lowest",  String.format("%.1f%%", stats.getMin()));
        setStat(statPass, "Passed",  passed + " (" + String.format("%.0f%%", passed * 100.0 / rs.size()) + ")");
        setStat(statFail, "Failed",  (rs.size() - passed) + "");
    }

    private void updateRoster(List<Result> rs, String query) {
        rosterModel.setRowCount(0);
        List<Result> sorted = rs.stream()
                .sorted(Comparator.comparingDouble(Result::getPercentage).reversed())
                .collect(Collectors.toList());
        int rank = 1;
        for (Result r : sorted) {
            rosterModel.addRow(new Object[]{
                    "#" + rank++,
                    r.getStudentName(),
                    r.getScore() + "/" + r.getTotalMarks(),
                    String.format("%.1f%%", r.getPercentage()),
                    r.getGrade(),
                    r.isPassed() ? "PASS" : "FAIL",
                    r.getAttemptDate()
            });
        }
    }

    private void updateGradeChart(List<Result> rs) {
        gradeBarPanel.removeAll();
        if (rs.isEmpty()) { gradeBarPanel.revalidate(); gradeBarPanel.repaint(); return; }

        Map<String, Long> dist = rs.stream()
                .collect(Collectors.groupingBy(Result::getGrade, Collectors.counting()));

        Color[] barColors = {
            UITheme.ACCENT_GREEN,
            new Color(52, 199, 123, 200),
            UITheme.ACCENT_BLUE,
            UITheme.ACCENT_ORANGE,
            new Color(200, 140, 50),
            UITheme.ACCENT_RED
        };

        String[] grades = {"A+", "A", "B", "C", "D", "F"};
        int total = rs.size();
        int ci = 0;
        for (String g : grades) {
            long count = dist.getOrDefault(g, 0L);
            int pct = total > 0 ? (int) (count * 100.0 / total) : 0;

            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBackground(UITheme.BG_PANEL);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

            JLabel gradeLabel = UITheme.makeLabel(g, UITheme.FONT_BTN, barColors[ci]);
            gradeLabel.setPreferredSize(new Dimension(24, 20));
            row.add(gradeLabel, BorderLayout.WEST);

            // Bar
            JPanel barOuter = new JPanel(new BorderLayout());
            barOuter.setBackground(UITheme.BG_DARK);
            Color bc = barColors[ci];
            JPanel barFill = new JPanel() {
                @Override protected void paintComponent(Graphics g2) {
                    super.paintComponent(g2);
                    Graphics2D g = (Graphics2D) g2;
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(bc);
                    g.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 6, 6);
                }
            };
            barFill.setOpaque(false);
            int barWidth = pct; // percent used as width constraint
            barFill.setPreferredSize(new Dimension(Math.max(4, barWidth * 2), 18));
            barOuter.add(barFill, BorderLayout.WEST);
            row.add(barOuter, BorderLayout.CENTER);

            JLabel countLbl = UITheme.makeLabel(count + " (" + pct + "%)",
                    UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
            countLbl.setPreferredSize(new Dimension(70, 20));
            row.add(countLbl, BorderLayout.EAST);

            gradeBarPanel.add(row);
            gradeBarPanel.add(Box.createVerticalStrut(4));
            ci++;
        }
        gradeBarPanel.revalidate();
        gradeBarPanel.repaint();
    }

    private void clearAll() {
        rosterModel.setRowCount(0);
        gradeBarPanel.removeAll();
        gradeBarPanel.revalidate();
        gradeBarPanel.repaint();
    }
}
