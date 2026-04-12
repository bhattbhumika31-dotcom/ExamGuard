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
 * Teacher UI for adding/editing both MCQ and text-based questions.
 */
public class QuestionPanel extends JPanel {

    private final ExamManager examManager;
    private final QuestionManager questionManager;
    private final String teacherId;

    private JComboBox<String> examCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel marksLabel;

    private static final String[] COLUMNS =
        {"Q#", "Type", "Question Text", "Choices", "Correct Answer", "Marks"};

    public QuestionPanel(ExamManager examManager, QuestionManager questionManager, String teacherId) {
        this.examManager = examManager;
        this.questionManager = questionManager;
        this.teacherId = teacherId;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(new EmptyBorder(20, 24, 12, 24));

        JLabel title = UITheme.makeTitle("Question Bank");
        JLabel subtitle = UITheme.makeMuted("Add MCQ questions with 2-4 choices or text-based questions");
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

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
        refreshExamsBtn.addActionListener(e -> {
            populateExamCombo();
            refreshTable();
        });
        selectorBar.add(refreshExamsBtn);

        marksLabel = UITheme.makeLabel("", UITheme.FONT_BODY, UITheme.ACCENT_GREEN);
        selectorBar.add(marksLabel);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(UITheme.BG_DARK);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(selectorBar, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(290);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);

        JScrollPane scroll = UITheme.makeScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 24, 0, 24));
        add(scroll, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        toolbar.setBackground(UITheme.BG_DARK);
        toolbar.setBorder(new EmptyBorder(0, 14, 10, 14));

        JButton addBtn = UITheme.makeSmallButton("+ Add Question", UITheme.ACCENT_BLUE);
        JButton editBtn = UITheme.makeSmallButton("✏ Edit", UITheme.ACCENT_ORANGE);
        JButton deleteBtn = UITheme.makeSmallButton("✕ Delete", UITheme.ACCENT_RED);
        JButton previewBtn = UITheme.makeSmallButton("⬜ Preview", new Color(70, 80, 110));

        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        previewBtn.addActionListener(e -> showPreview());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(previewBtn);
        add(toolbar, BorderLayout.SOUTH);

        populateExamCombo();
    }

    public void populateExamCombo() {
        examCombo.removeAllItems();
        List<Exam> exams = examManager.getExamsByTeacher(teacherId);
        for (Exam exam : exams) {
            examCombo.addItem(exam.getExamId() + " – " + exam.getTitle());
        }
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        marksLabel.setText("");

        String examId = getSelectedExamId();
        if (examId == null) {
            return;
        }

        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            return;
        }

        for (Question question : exam.getQuestions()) {
            String choices = question.isTextBased()
                ? "Text response"
                : String.join(" | ", question.getAllOptions());

            tableModel.addRow(new Object[]{
                "Q" + question.getQuestionId(),
                question.getQuestionType(),
                question.getQuestionText(),
                choices,
                question.getCorrectAnswerDisplay(),
                question.getMarks() + " pt"
            });
        }

        marksLabel.setText("  Total: " + exam.getTotalMarks() + " marks  |  "
            + exam.getQuestions().size() + " question(s)");
    }

    private String getSelectedExamId() {
        String selected = (String) examCombo.getSelectedItem();
        return selected == null ? null : selected.split("–")[0].trim();
    }

    private void openAddDialog() {
        String examId = getSelectedExamId();
        if (examId == null) {
            JOptionPane.showMessageDialog(this, "Select an exam first.");
            return;
        }

        QuestionFormDialog dialog = new QuestionFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), "Add Question", null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            questionManager.addQuestion(
                examId,
                dialog.getQuestionText(),
                dialog.getQuestionType(),
                dialog.getOptA(),
                dialog.getOptB(),
                dialog.getOptC(),
                dialog.getOptD(),
                dialog.getCorrectAnswerValue(),
                dialog.getMarks());
            refreshTable();
        }
    }

    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showSelectWarning();
            return;
        }

        String examId = getSelectedExamId();
        if (examId == null) {
            return;
        }

        String qLabel = (String) tableModel.getValueAt(row, 0);
        int qId = Integer.parseInt(qLabel.substring(1));

        Exam exam = examManager.getExamById(examId);
        Question question = exam.getQuestions().stream()
            .filter(value -> value.getQuestionId() == qId)
            .findFirst()
            .orElse(null);
        if (question == null) {
            return;
        }

        QuestionFormDialog dialog = new QuestionFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), "Edit Question", question);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            questionManager.editQuestion(
                examId,
                qId,
                dialog.getQuestionText(),
                dialog.getQuestionType(),
                dialog.getOptA(),
                dialog.getOptB(),
                dialog.getOptC(),
                dialog.getOptD(),
                dialog.getCorrectAnswerValue(),
                dialog.getMarks());
            refreshTable();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showSelectWarning();
            return;
        }

        String examId = getSelectedExamId();
        String qLabel = (String) tableModel.getValueAt(row, 0);
        int qId = Integer.parseInt(qLabel.substring(1));

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + qLabel + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            questionManager.deleteQuestion(examId, qId);
            refreshTable();
        }
    }

    private void showPreview() {
        String examId = getSelectedExamId();
        if (examId == null) {
            JOptionPane.showMessageDialog(this, "Select an exam first.");
            return;
        }

        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Segoe UI;padding:10px;color:#E8EAF0;background:#1C2436'>");
        html.append("<h2 style='color:#4285F4'>").append(escapeHtml(exam.getTitle())).append("</h2>");
        html.append("<p style='color:#9AA0AF'>Subject: ").append(escapeHtml(exam.getSubject()))
            .append(" | Duration: ").append(exam.getDurationMinutes())
            .append(" min | Total Marks: ").append(exam.getTotalMarks()).append("</p><hr>");

        int number = 1;
        for (Question question : exam.getQuestions()) {
            html.append("<p><b>").append(number++).append(". [")
                .append(question.getQuestionType()).append("] ")
                .append(escapeHtml(question.getQuestionText())).append("</b> ")
                .append("<span style='color:#9AA0AF'>[").append(question.getMarks()).append(" mark(s)]</span></p>");

            if (question.isTextBased()) {
                html.append("<p style='margin-left:20px'><i>Text response</i><br>")
                    .append("Expected answer: ").append(escapeHtml(question.getCorrectAnswerDisplay())).append("</p><hr>");
            } else {
                char letter = 'A';
                html.append("<p style='margin-left:20px'>");
                for (String option : question.getAllOptions()) {
                    html.append(letter).append(") ").append(escapeHtml(option)).append("<br>");
                    letter++;
                }
                html.append("Correct answer: ").append(escapeHtml(question.getCorrectAnswerDisplay())).append("</p><hr>");
            }
        }

        html.append("</body></html>");

        JEditorPane pane = new JEditorPane("text/html", html.toString());
        pane.setEditable(false);
        pane.setBackground(UITheme.BG_PANEL);

        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setPreferredSize(new Dimension(680, 500));

        JOptionPane.showMessageDialog(this, scrollPane,
            "Preview – " + exam.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br>");
    }

    private void showSelectWarning() {
        JOptionPane.showMessageDialog(this,
            "Please select a question from the table first.",
            "No Selection",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static class QuestionFormDialog extends JDialog {

        private boolean confirmed = false;

        private final JTextArea questionArea;
        private final JComboBox<String> typeCombo;
        private final JSpinner optionCountSpinner;
        private final JTextField[] optionFields;
        private final JLabel[] optionLabels;
        private final JComboBox<String> answerCombo;
        private final JTextField textAnswerField;
        private final JSpinner marksSpinner;
        private final JLabel optionCountLabel;
        private final JLabel correctChoiceLabel;
        private final JLabel textAnswerLabel;

        QuestionFormDialog(JFrame parent, String dialogTitle, Question existing) {
            super(parent, dialogTitle, true);
            setSize(640, 560);
            setLocationRelativeTo(parent);
            setResizable(false);
            getContentPane().setBackground(UITheme.BG_PANEL);
            setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UITheme.BG_PANEL);
            form.setBorder(new EmptyBorder(20, 24, 10, 24));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(7, 0, 7, 12);
            gc.anchor = GridBagConstraints.NORTHWEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1;

            questionArea = new JTextArea(3, 28);
            styleTextArea(questionArea);
            typeCombo = UITheme.makeComboBox(new String[]{"MCQ", "TEXT"});
            optionCountSpinner = UITheme.makeIntSpinner(2, 4, 4);
            optionFields = new JTextField[]{
                UITheme.makeTextField(28),
                UITheme.makeTextField(28),
                UITheme.makeTextField(28),
                UITheme.makeTextField(28)
            };
            optionLabels = new JLabel[4];
            answerCombo = UITheme.makeComboBox(new String[]{"A", "B", "C", "D"});
            textAnswerField = UITheme.makeTextField(28);
            marksSpinner = UITheme.makeIntSpinner(1, 100, existing == null ? 1 : existing.getMarks());

            int row = 0;
            addRow(form, gc, row++, "Question:", new JScrollPane(questionArea));
            addRow(form, gc, row++, "Question Type:", typeCombo);

            optionCountLabel = UITheme.makeLabel("Number of Choices:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
            addRow(form, gc, row++, optionCountLabel, optionCountSpinner);

            String[] optionNames = {"Option A:", "Option B:", "Option C:", "Option D:"};
            for (int i = 0; i < optionFields.length; i++) {
                optionLabels[i] = UITheme.makeLabel(optionNames[i], UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
                addRow(form, gc, row++, optionLabels[i], optionFields[i]);
            }

            correctChoiceLabel = UITheme.makeLabel("Correct Choice:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
            addRow(form, gc, row++, correctChoiceLabel, answerCombo);

            textAnswerLabel = UITheme.makeLabel("Expected Text Answer:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
            addRow(form, gc, row++, textAnswerLabel, textAnswerField);

            addRow(form, gc, row, "Marks:", marksSpinner);
            add(form, BorderLayout.CENTER);

            if (existing != null) {
                questionArea.setText(existing.getQuestionText());
                typeCombo.setSelectedItem(existing.getQuestionType());
                List<String> options = existing.getAllOptions();
                optionCountSpinner.setValue(Math.max(2, Math.min(4, options.isEmpty() ? 2 : options.size())));
                optionFields[0].setText(existing.getOptionA());
                optionFields[1].setText(existing.getOptionB());
                optionFields[2].setText(existing.getOptionC());
                optionFields[3].setText(existing.getOptionD());
                answerCombo.setSelectedItem(existing.getCorrectAnswerDisplay());
                textAnswerField.setText(existing.getCorrectAnswerText());
            }

            typeCombo.addActionListener(e -> updateQuestionMode());
            optionCountSpinner.addChangeListener(e -> updateOptionControls());
            updateQuestionMode();

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
            buttons.setBackground(UITheme.BG_PANEL);
            JButton cancel = UITheme.makeSmallButton("Cancel", new Color(80, 90, 120));
            JButton save = UITheme.makeSmallButton(existing == null ? "Add" : "Save", UITheme.ACCENT_BLUE);
            cancel.addActionListener(e -> dispose());
            save.addActionListener(e -> {
                if (!validateForm()) {
                    return;
                }
                confirmed = true;
                dispose();
            });
            buttons.add(cancel);
            buttons.add(save);
            add(buttons, BorderLayout.SOUTH);
        }

        private void addRow(JPanel form, GridBagConstraints gc, int row, String labelText, JComponent field) {
            JLabel label = UITheme.makeLabel(labelText, UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
            addRow(form, gc, row, label, field);
        }

        private void addRow(JPanel form, GridBagConstraints gc, int row, JLabel label, JComponent field) {
            gc.gridx = 0;
            gc.gridy = row;
            gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            form.add(label, gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            form.add(field, gc);
        }

        private void styleTextArea(JTextArea area) {
            area.setBackground(UITheme.BG_DARK);
            area.setForeground(UITheme.TEXT_PRIMARY);
            area.setCaretColor(UITheme.TEXT_PRIMARY);
            area.setFont(UITheme.FONT_BODY);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)));
        }

        private void updateQuestionMode() {
            boolean mcqMode = "MCQ".equals(getQuestionType());

            optionCountLabel.setVisible(mcqMode);
            optionCountSpinner.setVisible(mcqMode);
            correctChoiceLabel.setVisible(mcqMode);
            answerCombo.setVisible(mcqMode);
            textAnswerLabel.setVisible(!mcqMode);
            textAnswerField.setVisible(!mcqMode);

            updateOptionControls();
        }

        private void updateOptionControls() {
            boolean mcqMode = "MCQ".equals(getQuestionType());
            int optionCount = (Integer) optionCountSpinner.getValue();

            for (int i = 0; i < optionFields.length; i++) {
                boolean visible = mcqMode && i < optionCount;
                optionLabels[i].setVisible(visible);
                optionFields[i].setVisible(visible);
            }

            Object selected = answerCombo.getSelectedItem();
            answerCombo.removeAllItems();
            for (int i = 0; i < optionCount; i++) {
                answerCombo.addItem(String.valueOf((char) ('A' + i)));
            }
            if (selected != null) {
                answerCombo.setSelectedItem(selected.toString());
            }
            if (answerCombo.getSelectedItem() == null && answerCombo.getItemCount() > 0) {
                answerCombo.setSelectedIndex(0);
            }

            revalidate();
            repaint();
        }

        private boolean validateForm() {
            if (getQuestionText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Question text is required.");
                return false;
            }

            if ("TEXT".equals(getQuestionType())) {
                if (getCorrectAnswerValue().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Provide the expected answer for the text question.");
                    return false;
                }
                return true;
            }

            int optionCount = (Integer) optionCountSpinner.getValue();
            for (int i = 0; i < optionCount; i++) {
                if (optionFields[i].getText().isBlank()) {
                    JOptionPane.showMessageDialog(this,
                        "Fill in all visible MCQ choices. At least 2 choices are required.");
                    return false;
                }
            }
            return true;
        }

        boolean isConfirmed() { return confirmed; }
        String getQuestionText() { return questionArea.getText().trim(); }
        String getQuestionType() {
            Object selected = typeCombo.getSelectedItem();
            return selected == null ? "MCQ" : selected.toString();
        }
        String getOptA() { return getOptionValue(0); }
        String getOptB() { return getOptionValue(1); }
        String getOptC() { return getOptionValue(2); }
        String getOptD() { return getOptionValue(3); }
        String getCorrectAnswerValue() {
            if ("TEXT".equals(getQuestionType())) {
                return textAnswerField.getText().trim();
            }
            Object selected = answerCombo.getSelectedItem();
            return selected == null ? "A" : selected.toString();
        }
        int getMarks() { return (Integer) marksSpinner.getValue(); }

        private String getOptionValue(int index) {
            if (!"MCQ".equals(getQuestionType()) || !optionFields[index].isVisible()) {
                return "";
            }
            return optionFields[index].getText().trim();
        }
    }
}
