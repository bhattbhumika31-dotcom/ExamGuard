package teacher.panels;

import model.Exam;
import model.Question;
import teacher.ExamManager;
import teacher.QuestionManager;
import teacher.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ExamGuard – teacher/panels/QuestionPanel.java
 *
 * JPanel for the "Manage Questions" tab.
 *
 * Flow:
 *   1. Teacher picks an exam from the top combo-box.
 *   2. The table below shows all questions in that exam.
 *   3. Toolbar buttons: Add | Edit | Delete | Preview Exam
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class QuestionPanel extends JPanel {

    // ------------------------------------------------------------------ //
    //  Fields
    // ------------------------------------------------------------------ //

    private final ExamManager     examManager;
    private final QuestionManager questionManager;
    private final String          teacherId;

    private JComboBox<String>  examCombo;
    private JTable             table;
    private DefaultTableModel  tableModel;
    private JLabel             marksLabel;

    private static final String[] COLUMNS =
            {"Q#", "Question Text", "Opt A", "Opt B", "Opt C", "Opt D", "Answer", "Marks"};

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public QuestionPanel(ExamManager examManager, QuestionManager questionManager,
                         String teacherId) {
        this.examManager     = examManager;
        this.questionManager = questionManager;
        this.teacherId       = teacherId;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    // ------------------------------------------------------------------ //
    //  UI Construction
    // ------------------------------------------------------------------ //

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(new EmptyBorder(20, 24, 12, 24));

        JLabel title = UITheme.makeTitle("Question Bank");
        JLabel sub   = UITheme.makeMuted("Add and manage MCQ questions per exam");
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);
        header.add(titles, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Exam Selector Bar ────────────────────────────────────────────
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        selectorBar.setBackground(UITheme.BG_PANEL);
        selectorBar.setBorder(new EmptyBorder(4, 24, 4, 24));

        selectorBar.add(UITheme.makeLabel("Select Exam:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));

        examCombo = new JComboBox<>();
        examCombo.setBackground(UITheme.BG_DARK);
        examCombo.setForeground(UITheme.TEXT_PRIMARY);
        examCombo.setFont(UITheme.FONT_BODY);
        examCombo.setPreferredSize(new Dimension(280, 32));
        examCombo.addActionListener(e -> refreshTable());
        selectorBar.add(examCombo);

        JButton refreshExamsBtn = UITheme.makeSmallButton("↻ Load Exams", new Color(70, 80, 110));
        refreshExamsBtn.addActionListener(e -> { populateExamCombo(); refreshTable(); });
        selectorBar.add(refreshExamsBtn);

        marksLabel = UITheme.makeLabel("", UITheme.FONT_BODY, UITheme.ACCENT_GREEN);
        selectorBar.add(marksLabel);

        add(selectorBar, BorderLayout.NORTH);

        // We need a proper center layout including header + selectorBar
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(UITheme.BG_DARK);
        topSection.add(header,      BorderLayout.NORTH);
        topSection.add(selectorBar, BorderLayout.SOUTH);
        remove(header);
        add(topSection, BorderLayout.NORTH);

        // ── Table ───────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Wrap question text
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(6).setPreferredWidth(55);
        table.getColumnModel().getColumn(7).setPreferredWidth(55);

        // Highlight correct answer column
        table.getColumnModel().getColumn(6).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(val == null ? "" : val.toString(), SwingConstants.CENTER);
            lbl.setFont(UITheme.FONT_BTN);
            lbl.setForeground(UITheme.ACCENT_GREEN);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.ACCENT_BLUE.darker()
                    : (row % 2 == 0 ? UITheme.TABLE_ROW_EVEN : UITheme.TABLE_ROW_ODD));
            return lbl;
        });

        JScrollPane scroll = UITheme.makeScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 24, 0, 24));
        add(scroll, BorderLayout.CENTER);

        // ── Toolbar ─────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        toolbar.setBackground(UITheme.BG_DARK);
        toolbar.setBorder(new EmptyBorder(0, 14, 10, 14));

        JButton addBtn     = UITheme.makeSmallButton("+ Add Question",  UITheme.ACCENT_BLUE);
        JButton editBtn    = UITheme.makeSmallButton("✏  Edit",         UITheme.ACCENT_ORANGE);
        JButton deleteBtn  = UITheme.makeSmallButton("✕  Delete",        UITheme.ACCENT_RED);
        JButton previewBtn = UITheme.makeSmallButton("⬜  Preview",      new Color(70, 80, 110));

        addBtn.addActionListener(e    -> openAddDialog());
        editBtn.addActionListener(e   -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        previewBtn.addActionListener(e -> showPreview());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(previewBtn);
        add(toolbar, BorderLayout.SOUTH);

        // Initial load
        populateExamCombo();
    }

    // ------------------------------------------------------------------ //
    //  Combo & Table Refresh
    // ------------------------------------------------------------------ //

    public void populateExamCombo() {
        examCombo.removeAllItems();
        List<Exam> exams = examManager.getExamsByTeacher(teacherId);
        for (Exam e : exams) {
            examCombo.addItem(e.getExamId() + " – " + e.getTitle());
        }
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        marksLabel.setText("");
        String selected = (String) examCombo.getSelectedItem();
        if (selected == null) return;
        String examId = selected.split("–")[0].trim();
        Exam exam = examManager.getExamById(examId);
        if (exam == null) return;

        for (Question q : exam.getQuestions()) {
            tableModel.addRow(new Object[]{
                    "Q" + q.getQuestionId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    String.valueOf(q.getCorrectAnswer()),
                    q.getMarks() + " pt"
            });
        }
        marksLabel.setText("  Total: " + exam.getTotalMarks() + " marks  |  "
                + exam.getQuestions().size() + " question(s)");
    }

    private String getSelectedExamId() {
        String s = (String) examCombo.getSelectedItem();
        return s == null ? null : s.split("–")[0].trim();
    }

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //

    private void openAddDialog() {
        String examId = getSelectedExamId();
        if (examId == null) { JOptionPane.showMessageDialog(this, "Select an exam first."); return; }

        QuestionFormDialog dlg = new QuestionFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), "Add Question", null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            questionManager.addQuestion(examId,
                    dlg.getQuestionText(),
                    dlg.getOptA(), dlg.getOptB(), dlg.getOptC(), dlg.getOptD(),
                    dlg.getCorrectAnswer(), dlg.getMarks());
            refreshTable();
        }
    }

    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { showSelectWarning(); return; }
        String examId = getSelectedExamId();
        if (examId == null) return;

        // Parse Q# back to int
        String qLabel = (String) tableModel.getValueAt(row, 0); // e.g. "Q3"
        int qId = Integer.parseInt(qLabel.substring(1));

        Exam     exam = examManager.getExamById(examId);
        Question q    = exam.getQuestions().stream()
                            .filter(x -> x.getQuestionId() == qId)
                            .findFirst().orElse(null);
        if (q == null) return;

        QuestionFormDialog dlg = new QuestionFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), "Edit Question", q);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            questionManager.editQuestion(examId, qId,
                    dlg.getQuestionText(),
                    dlg.getOptA(), dlg.getOptB(), dlg.getOptC(), dlg.getOptD(),
                    dlg.getCorrectAnswer(), dlg.getMarks());
            refreshTable();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showSelectWarning(); return; }
        String examId  = getSelectedExamId();
        String qLabel  = (String) tableModel.getValueAt(row, 0);
        int    qId     = Integer.parseInt(qLabel.substring(1));

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete " + qLabel + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            questionManager.deleteQuestion(examId, qId);
            refreshTable();
        }
    }

    private void showPreview() {
        String examId = getSelectedExamId();
        if (examId == null) { JOptionPane.showMessageDialog(this, "Select an exam first."); return; }
        Exam exam = examManager.getExamById(examId);
        if (exam == null) return;

        // Build preview text
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI;padding:10px;color:#E8EAF0;background:#1C2436'>");
        sb.append("<h2 style='color:#4285F4'>").append(exam.getTitle()).append("</h2>");
        sb.append("<p style='color:#9AA0AF'>Subject: ").append(exam.getSubject())
          .append("  |  Duration: ").append(exam.getDurationMinutes())
          .append(" min  |  Total Marks: ").append(exam.getTotalMarks()).append("</p><hr>");

        int num = 1;
        for (Question q : exam.getQuestions()) {
            sb.append("<p><b>").append(num++).append(". ").append(q.getQuestionText())
              .append("</b> <span style='color:#9AA0AF'>[").append(q.getMarks()).append(" mark(s)]</span></p>");
            sb.append("<p style='margin-left:20px'>A) ").append(q.getOptionA()).append("<br>")
              .append("B) ").append(q.getOptionB()).append("<br>")
              .append("C) ").append(q.getOptionC()).append("<br>")
              .append("D) ").append(q.getOptionD()).append("</p><hr>");
        }
        sb.append("</body></html>");

        JEditorPane ep = new JEditorPane("text/html", sb.toString());
        ep.setEditable(false);
        ep.setBackground(UITheme.BG_PANEL);
        JScrollPane sp = new JScrollPane(ep);
        sp.setPreferredSize(new Dimension(620, 480));

        JOptionPane.showMessageDialog(this, sp,
                "Preview – " + exam.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    private void showSelectWarning() {
        JOptionPane.showMessageDialog(this,
                "Please select a question from the table first.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }

    // ================================================================== //
    //  Inner Dialog – Add / Edit Question
    // ================================================================== //

    private static class QuestionFormDialog extends JDialog {

        private boolean    confirmed = false;
        private final JTextField  qtextField, optAField, optBField, optCField, optDField;
        private final JComboBox<String> answerCombo;
        private final JSpinner    marksSpinner;

        QuestionFormDialog(JFrame parent, String dialogTitle, Question existing) {
            super(parent, dialogTitle, true);
            setSize(560, 460);
            setLocationRelativeTo(parent);
            setResizable(false);
            getContentPane().setBackground(UITheme.BG_PANEL);
            setLayout(new BorderLayout());

            // ── Form Grid ────────────────────────────────────────────────
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UITheme.BG_PANEL);
            form.setBorder(new EmptyBorder(20, 28, 10, 28));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(7, 0, 7, 12);
            gc.anchor = GridBagConstraints.WEST;

            qtextField  = UITheme.makeTextField(30);
            optAField   = UITheme.makeTextField(30);
            optBField   = UITheme.makeTextField(30);
            optCField   = UITheme.makeTextField(30);
            optDField   = UITheme.makeTextField(30);
            answerCombo = UITheme.makeComboBox(new String[]{"A", "B", "C", "D"});
            marksSpinner = UITheme.makeIntSpinner(1, 100, 1);

            if (existing != null) {
                qtextField.setText(existing.getQuestionText());
                optAField.setText(existing.getOptionA());
                optBField.setText(existing.getOptionB());
                optCField.setText(existing.getOptionC());
                optDField.setText(existing.getOptionD());
                answerCombo.setSelectedItem(String.valueOf(existing.getCorrectAnswer()));
                marksSpinner.setValue(existing.getMarks());
            }

            String[]     labels = {"Question:", "Option A:", "Option B:", "Option C:", "Option D:", "Correct Answer:", "Marks:"};
            JComponent[] fields = {qtextField, optAField, optBField, optCField, optDField, answerCombo, marksSpinner};

            for (int i = 0; i < labels.length; i++) {
                gc.gridx = 0; gc.gridy = i; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
                form.add(UITheme.makeLabel(labels[i], UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gc);
                gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
                form.add(fields[i], gc);
            }
            add(form, BorderLayout.CENTER);

            // ── Buttons ──────────────────────────────────────────────────
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
            btns.setBackground(UITheme.BG_PANEL);
            JButton cancel = UITheme.makeSmallButton("Cancel", new Color(80, 90, 120));
            JButton save   = UITheme.makeSmallButton(existing == null ? "Add" : "Save",
                                                     UITheme.ACCENT_BLUE);
            cancel.addActionListener(e -> dispose());
            save.addActionListener(e -> {
                if (qtextField.getText().isBlank() || optAField.getText().isBlank()
                        || optBField.getText().isBlank() || optCField.getText().isBlank()
                        || optDField.getText().isBlank()) {
                    JOptionPane.showMessageDialog(this, "All fields are required.");
                    return;
                }
                confirmed = true;
                dispose();
            });
            btns.add(cancel);
            btns.add(save);
            add(btns, BorderLayout.SOUTH);
        }

        boolean isConfirmed()     { return confirmed; }
        String  getQuestionText() { return qtextField.getText().trim(); }
        String  getOptA()         { return optAField.getText().trim(); }
        String  getOptB()         { return optBField.getText().trim(); }
        String  getOptC()         { return optCField.getText().trim(); }
        String  getOptD()         { return optDField.getText().trim(); }
        char    getCorrectAnswer(){ 
            Object sel = answerCombo.getSelectedItem();
            return sel == null ? 'A' : sel.toString().charAt(0); 
        }
        int     getMarks()        { return (Integer) marksSpinner.getValue(); }
    }
}
