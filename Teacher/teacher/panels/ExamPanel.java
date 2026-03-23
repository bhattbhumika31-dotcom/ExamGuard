package teacher.panels;

import model.Exam;
import teacher.ExamManager;
import teacher.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ExamGuard – teacher/panels/ExamPanel.java
 *
 * JPanel that handles the "Manage Exams" tab in the Teacher Dashboard.
 * Displays a live table of the teacher's exams and provides buttons to:
 *   Create | Edit | Delete | Activate/Deactivate
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public class ExamPanel extends JPanel {

    // ------------------------------------------------------------------ //
    //  Fields
    // ------------------------------------------------------------------ //

    private final ExamManager examManager;
    private final String      teacherId;
    private final String      teacherName;

    private JTable         table;
    private DefaultTableModel tableModel;

    private static final String[] COLUMNS =
            {"Exam ID", "Title", "Subject", "Duration (min)", "Questions", "Total Marks", "Status"};

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public ExamPanel(ExamManager examManager, String teacherId, String teacherName) {
        this.examManager = examManager;
        this.teacherId   = teacherId;
        this.teacherName = teacherName;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        refreshTable();
    }

    // ------------------------------------------------------------------ //
    //  UI Construction
    // ------------------------------------------------------------------ //

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(new javax.swing.border.EmptyBorder(20, 24, 12, 24));

        JLabel title = UITheme.makeTitle("Exam Management");
        JLabel sub   = UITheme.makeMuted("Create, edit, and publish your exams");
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);
        header.add(titles, BorderLayout.WEST);

        JButton createBtn = UITheme.makeButton("+ New Exam", UITheme.ACCENT_BLUE);
        createBtn.addActionListener(e -> openCreateDialog());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(createBtn);
        header.add(btnWrap, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Table ───────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Colour the Status column
        table.getColumnModel().getColumn(6).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(val == null ? "" : val.toString(), SwingConstants.CENTER);
            boolean active = "ACTIVE".equals(val);
            lbl.setForeground(active ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
            lbl.setFont(UITheme.FONT_BTN);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.TABLE_SELECT : (row % 2 == 0 ? UITheme.TABLE_ROW_EVEN : UITheme.TABLE_ROW_ODD));
            return lbl;
        });

        // Alternate row colours
        table.setDefaultRenderer(Object.class, (tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(val == null ? "" : val.toString());
            lbl.setFont(UITheme.FONT_BODY);
            lbl.setBorder(new javax.swing.border.EmptyBorder(0, 10, 0, 10));
            lbl.setOpaque(true);
            lbl.setForeground(sel ? Color.WHITE : UITheme.TEXT_PRIMARY);
            lbl.setBackground(sel ? UITheme.ACCENT_BLUE.darker()
                    : (row % 2 == 0 ? UITheme.TABLE_ROW_EVEN : UITheme.TABLE_ROW_ODD));
            return lbl;
        });

        JScrollPane scroll = UITheme.makeScrollPane(table);
        scroll.setBorder(new javax.swing.border.EmptyBorder(0, 24, 0, 24));

        add(scroll, BorderLayout.CENTER);

        // ── Action Toolbar ───────────────────────────────────────────────
        JPanel toolbar = buildToolbar();
        add(toolbar, BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        bar.setBackground(UITheme.BG_DARK);
        bar.setBorder(new javax.swing.border.EmptyBorder(0, 14, 10, 14));

        JButton editBtn     = UITheme.makeSmallButton("✏  Edit",          UITheme.ACCENT_ORANGE);
        JButton toggleBtn   = UITheme.makeSmallButton("⦿  Toggle Status", UITheme.ACCENT_BLUE);
        JButton deleteBtn   = UITheme.makeSmallButton("✕  Delete",         UITheme.ACCENT_RED);
        JButton refreshBtn  = UITheme.makeSmallButton("↻  Refresh",        new Color(70, 80, 110));

        editBtn.addActionListener(e -> openEditDialog());
        toggleBtn.addActionListener(e -> toggleSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> refreshTable());

        bar.add(editBtn);
        bar.add(toggleBtn);
        bar.add(deleteBtn);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(refreshBtn);

        return bar;
    }

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //

    private void openCreateDialog() {
        ExamFormDialog dlg = new ExamFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Create New Exam", null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            examManager.createExam(
                    dlg.getExamTitle(),
                    dlg.getSubject(),
                    dlg.getDuration(),
                    teacherId, teacherName);
            refreshTable();
        }
    }

    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { showSelectWarning(); return; }

        String examId = (String) tableModel.getValueAt(row, 0);
        Exam   exam   = examManager.getExamById(examId);
        if (exam == null) return;

        ExamFormDialog dlg = new ExamFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Edit Exam", exam);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            examManager.editExam(examId, dlg.getExamTitle(), dlg.getSubject(), dlg.getDuration());
            refreshTable();
        }
    }

    private void toggleSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showSelectWarning(); return; }
        String examId = (String) tableModel.getValueAt(row, 0);
        examManager.toggleExamStatus(examId);
        refreshTable();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showSelectWarning(); return; }
        String examId = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Delete exam <b>" + examId + "</b>?<br>"
                + "All questions in this exam will also be removed.</html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            examManager.deleteExam(examId, teacherId);
            refreshTable();
        }
    }

    // ------------------------------------------------------------------ //
    //  Table refresh
    // ------------------------------------------------------------------ //

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Exam> exams = examManager.getExamsByTeacher(teacherId);
        for (Exam e : exams) {
            tableModel.addRow(new Object[]{
                    e.getExamId(),
                    e.getTitle(),
                    e.getSubject(),
                    e.getDurationMinutes() + " min",
                    e.getQuestions().size(),
                    e.getTotalMarks(),
                    e.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
    }

    /** Returns the currently selected exam's ID, or null if nothing selected. */
    public String getSelectedExamId() {
        int row = table.getSelectedRow();
        return row >= 0 ? (String) tableModel.getValueAt(row, 0) : null;
    }

    private void showSelectWarning() {
        JOptionPane.showMessageDialog(this,
                "Please select an exam from the table first.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }

    // ================================================================== //
    //  Inner Dialog – Create / Edit Exam
    // ================================================================== //

    private static class ExamFormDialog extends JDialog {

        private boolean confirmed = false;
        private final JTextField  titleField;
        private final JTextField  subjectField;
        private final JSpinner    durationSpinner;

        ExamFormDialog(JFrame parent, String dialogTitle, Exam existing) {
            super(parent, dialogTitle, true);
            setSize(440, 320);
            setLocationRelativeTo(parent);
            setResizable(false);
            getContentPane().setBackground(UITheme.BG_PANEL);
            setLayout(new BorderLayout());

            // ── Form ────────────────────────────────────────────────────
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UITheme.BG_PANEL);
            form.setBorder(new javax.swing.border.EmptyBorder(20, 30, 10, 30));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets  = new Insets(8, 0, 8, 12);
            gc.anchor  = GridBagConstraints.WEST;

            titleField      = UITheme.makeTextField(20);
            subjectField    = UITheme.makeTextField(20);
            durationSpinner = UITheme.makeIntSpinner(5, 300, 60);

            if (existing != null) {
                titleField.setText(existing.getTitle());
                subjectField.setText(existing.getSubject());
                durationSpinner.setValue(existing.getDurationMinutes());
            }

            String[][] rows = {
                    {"Exam Title",      null},
                    {"Subject",         null},
                    {"Duration (min)",  null}
            };
            JComponent[] fields = {titleField, subjectField, durationSpinner};
            String[] labels = {"Exam Title :", "Subject :", "Duration (min) :"};

            for (int i = 0; i < labels.length; i++) {
                gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
                JLabel lbl = UITheme.makeLabel(labels[i], UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
                form.add(lbl, gc);
                gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
                form.add(fields[i], gc);
                gc.fill = GridBagConstraints.NONE;
            }
            add(form, BorderLayout.CENTER);

            // ── Buttons ──────────────────────────────────────────────────
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
            btns.setBackground(UITheme.BG_PANEL);
            JButton cancel = UITheme.makeSmallButton("Cancel", new Color(80, 90, 120));
            JButton save   = UITheme.makeSmallButton(existing == null ? "Create" : "Save",
                                                     UITheme.ACCENT_BLUE);
            cancel.addActionListener(e -> dispose());
            save.addActionListener(e -> {
                if (titleField.getText().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Title cannot be empty.");
                    return;
                }
                confirmed = true;
                dispose();
            });
            btns.add(cancel);
            btns.add(save);
            add(btns, BorderLayout.SOUTH);
        }

        boolean isConfirmed()   { return confirmed; }
        String  getExamTitle()  { return titleField.getText().trim(); }
        String  getSubject()    { return subjectField.getText().trim(); }
        int     getDuration()   { return (Integer) durationSpinner.getValue(); }
    }
}
