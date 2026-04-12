import core.ExamGuardRepository;
import core.ExamProgressStore;
import model.Exam;
import model.Question;
import model.Result;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StudentService {

    private static final DateTimeFormatter ATTEMPT_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String REATTEMPT_NOT_ALLOWED_MESSAGE =
        "You have already submitted this exam. Re-attempts are not allowed.";

    private final ExamGuardRepository repository = ExamGuardRepository.getInstance();

    public Result startExam(String studentId, String examId) {
        return startExam(studentId, studentId, examId);
    }

    public Result startExam(String studentId, String studentName, String examId) {
        Exam exam = validateExam(examId);
        if (exam == null) {
            return null;
        }
        if (repository.hasResultForStudentExam(studentId, exam.getExamId())) {
            ExamProgressStore.clearProgress(studentId, exam.getExamId());
            printReattemptDenied(exam);
            return null;
        }

        Scanner scanner = new Scanner(System.in);
        AttemptContext attempt = initializeAttempt(studentId, studentName, exam, null, scanner);
        if (attempt == null) {
            return null;
        }

        System.out.println("\nExam started successfully: " + exam.getTitle());
        System.out.println("Subject: " + exam.getSubject());
        System.out.println("Duration: " + exam.getDurationMinutes() + " minutes");

        if (attempt.startIndex > 0) {
            System.out.println("Resuming your saved progress from question " + (attempt.startIndex + 1) + ".");
        }

        for (int i = attempt.startIndex; i < exam.getQuestions().size(); i++) {
            long remainingSeconds = getRemainingSeconds(exam, attempt.startedAt);
            if (remainingSeconds <= 0) {
                System.out.println("\nTime is up. Auto-submitting the exam.");
                break;
            }

            Question question = exam.getQuestions().get(i);
            displayQuestion(question);
            System.out.println("Time Left: " + formatRemainingTime(remainingSeconds));
            System.out.print(question.isTextBased()
                ? "Your answer: "
                : "Your answer (A/B/C/D, leave blank to skip): ");

            String answer = scanner.nextLine().trim();
            if (question.isMcq()) {
                answer = answer.toUpperCase();
            }

            attempt.answers.put(question.getQuestionId(), answer);
            saveAttemptProgress(studentId, attempt.studentName, exam, i + 1, attempt.startedAt, attempt.answers);
        }

        return finalizeAttempt(studentId, attempt.studentName, exam, attempt.answers, attempt.startedAt, null);
    }

    public Result startExamWithDialog(Component parent, String studentId, String studentName, String examId) {
        Exam exam = validateExam(examId);
        if (exam == null) {
            JOptionPane.showMessageDialog(parent, "Exam not found or inactive.");
            return null;
        }
        if (exam.getQuestions().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "This exam has no questions yet.");
            return null;
        }
        if (repository.hasResultForStudentExam(studentId, exam.getExamId())) {
            ExamProgressStore.clearProgress(studentId, exam.getExamId());
            showReattemptDenied(parent, exam);
            return null;
        }

        AttemptContext attempt = initializeAttempt(studentId, studentName, exam, parent, null);
        if (attempt == null) {
            return null;
        }

        if (getRemainingSeconds(exam, attempt.startedAt) <= 0) {
            JOptionPane.showMessageDialog(parent,
                "The exam time is already over. Your saved answers will be submitted automatically.",
                "Time Up",
                JOptionPane.WARNING_MESSAGE);
            return finalizeAttempt(studentId, attempt.studentName, exam, attempt.answers, attempt.startedAt, parent);
        }

        DialogAttemptResult dialogResult = showExamAttemptDialog(parent, studentId, exam, attempt);
        if (dialogResult == null || !dialogResult.submitted) {
            return null;
        }

        return finalizeAttempt(studentId, dialogResult.studentName, exam, dialogResult.answers, dialogResult.startedAt, parent);
    }

    public List<Exam> getAvailableExams() {
        return repository.getActiveExams();
    }

    public List<Result> getStudentResults(String studentId) {
        return repository.getResultsForStudent(studentId);
    }

    public boolean hasSubmittedExam(String studentId, String examId) {
        boolean submitted = repository.hasResultForStudentExam(studentId, examId);
        if (submitted) {
            ExamProgressStore.clearProgress(studentId, examId);
        }
        return submitted;
    }

    public String getExamStatus(String studentId, String examId) {
        if (hasSubmittedExam(studentId, examId)) {
            return "Submitted";
        }

        ExamProgressStore.SavedProgress savedProgress = ExamProgressStore.loadProgress(studentId, examId);
        if (savedProgress != null) {
            return "In Progress";
        }

        return "Ready";
    }

    private Exam validateExam(String examId) {
        Exam exam = repository.findExam(examId);
        if (exam == null || !exam.isActive()) {
            System.out.println("Sorry, exam not found or inactive.");
            return null;
        }
        if (exam.getQuestions().isEmpty()) {
            System.out.println("This exam has no questions yet.");
            return null;
        }
        return exam;
    }

    private Result buildResult(String studentId, String studentName, Exam exam, int score, long startedAt) {
        long elapsedSeconds = Math.max(1, (System.currentTimeMillis() - startedAt) / 1000);
        String resultId = "RES-" + exam.getExamId() + "-" + studentId;

        return new Result(
            resultId,
            studentId,
            studentName,
            exam.getExamId(),
            exam.getTitle(),
            score,
            exam.getTotalMarks(),
            elapsedSeconds,
            LocalDateTime.now().format(ATTEMPT_FORMAT));
    }

    private AttemptContext initializeAttempt(String studentId, String studentName, Exam exam, Component parent, Scanner scanner) {
        Map<Integer, String> answers = new LinkedHashMap<>();
        long startedAt = System.currentTimeMillis();
        int startIndex = 0;
        String resolvedStudentName = studentName;

        ExamProgressStore.SavedProgress savedProgress = ExamProgressStore.loadProgress(studentId, exam.getExamId());
        if (savedProgress != null && !savedProgress.getAnswers().isEmpty()) {
            boolean resumeSavedProgress = shouldResumeSavedProgress(parent, scanner, exam.getTitle());
            if (resumeSavedProgress) {
                answers.putAll(savedProgress.getAnswers());
                startedAt = savedProgress.getStartedAtMillis() > 0 ? savedProgress.getStartedAtMillis() : startedAt;
                startIndex = Math.min(savedProgress.getCurrentQuestionIndex(), exam.getQuestions().size());
                if (savedProgress.getStudentName() != null && !savedProgress.getStudentName().isBlank()) {
                    resolvedStudentName = savedProgress.getStudentName();
                }
            } else {
                ExamProgressStore.clearProgress(studentId, exam.getExamId());
            }
        }

        return new AttemptContext(resolvedStudentName, startedAt, startIndex, answers);
    }

    private boolean shouldResumeSavedProgress(Component parent, Scanner scanner, String examTitle) {
        if (parent != null) {
            int choice = JOptionPane.showConfirmDialog(
                parent,
                "Saved progress was found for '" + examTitle + "'. Resume from where you left off?",
                "Resume Exam",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            return choice == JOptionPane.YES_OPTION;
        }

        System.out.print("Saved progress found for '" + examTitle + "'. Resume? (Y/N): ");
        String decision = scanner != null ? scanner.nextLine().trim().toUpperCase() : "N";
        return decision.startsWith("Y");
    }

    private void saveAttemptProgress(String studentId, String studentName, Exam exam,
                                     int nextQuestionIndex, long startedAt, Map<Integer, String> answers) {
        ExamProgressStore.saveProgress(
            studentId,
            studentName,
            exam.getExamId(),
            exam.getTitle(),
            nextQuestionIndex,
            startedAt,
            answers);
    }

    private Result finalizeAttempt(String studentId, String studentName, Exam exam,
                                   Map<Integer, String> answers, long startedAt, Component parent) {
        int score = calculateScore(exam, answers);
        Result result = buildResult(studentId, studentName, exam, score, startedAt);
        ExamProgressStore.clearProgress(studentId, exam.getExamId());
        if (!repository.addOrUpdateResult(result)) {
            if (parent == null) {
                printReattemptDenied(exam);
            } else {
                showReattemptDenied(parent, exam);
            }
            return repository.findResultForStudentExam(studentId, exam.getExamId());
        }

        if (parent == null) {
            printSubmissionSummary(result);
        } else {
            JOptionPane.showMessageDialog(
                parent,
                "Exam submitted.\nScore: " + result.getScore() + "/" + result.getTotalMarks()
                    + "\nPercentage: " + String.format("%.1f%%", result.getPercentage())
                    + "\nGrade: " + result.getGrade(),
                "Submission Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }

        return result;
    }

    private DialogAttemptResult showExamAttemptDialog(Component parent, String studentId, Exam exam, AttemptContext attempt) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Attempt Exam - " + exam.getTitle(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(1020, 660);
        dialog.setMinimumSize(new Dimension(900, 560));
        dialog.setLocationRelativeTo(parent);

        Map<Integer, String> workingAnswers = new LinkedHashMap<>(attempt.answers);
        List<Question> questions = exam.getQuestions();
        int[] currentIndex = {Math.max(0, Math.min(attempt.startIndex, questions.size() - 1))};
        boolean[] submitted = {false};

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.setBackground(new Color(25, 30, 50));

        JLabel headerLabel = new JLabel();
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel timerLabel = new JLabel();
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel topInfoPanel = new JPanel(new BorderLayout());
        topInfoPanel.setOpaque(false);
        topInfoPanel.add(headerLabel, BorderLayout.WEST);
        topInfoPanel.add(timerLabel, BorderLayout.EAST);

        JTextArea questionArea = new JTextArea();
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        questionArea.setBackground(new Color(30, 35, 55));
        questionArea.setForeground(Color.WHITE);
        questionArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel optionPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        optionPanel.setOpaque(false);

        ButtonGroup answerGroup = new ButtonGroup();
        JRadioButton[] optionButtons = new JRadioButton[4];
        for (int i = 0; i < optionButtons.length; i++) {
            JRadioButton optionButton = new JRadioButton();
            optionButton.setOpaque(true);
            optionButton.setBackground(new Color(40, 45, 70));
            optionButton.setForeground(Color.WHITE);
            optionButton.setFocusPainted(false);
            optionButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            optionButtons[i] = optionButton;
            answerGroup.add(optionButton);
            optionPanel.add(optionButton);
        }

        JTextArea textAnswerArea = new JTextArea(5, 24);
        textAnswerArea.setLineWrap(true);
        textAnswerArea.setWrapStyleWord(true);
        textAnswerArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textAnswerArea.setBackground(new Color(40, 45, 70));
        textAnswerArea.setForeground(Color.WHITE);
        textAnswerArea.setCaretColor(Color.WHITE);
        textAnswerArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel textPanel = new JPanel(new BorderLayout(0, 8));
        textPanel.setOpaque(false);
        JLabel textAnswerLabel = new JLabel("Type your answer below:");
        textAnswerLabel.setForeground(Color.WHITE);
        textPanel.add(textAnswerLabel, BorderLayout.NORTH);
        textPanel.add(new JScrollPane(textAnswerArea), BorderLayout.CENTER);

        CardLayout answerCardLayout = new CardLayout();
        JPanel answerCardPanel = new JPanel(answerCardLayout);
        answerCardPanel.setOpaque(false);
        answerCardPanel.add(optionPanel, "MCQ");
        answerCardPanel.add(textPanel, "TEXT");

        DefaultListModel<String> questionListModel = new DefaultListModel<>();
        JList<String> questionList = new JList<>(questionListModel);
        questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionList.setBackground(new Color(20, 25, 45));
        questionList.setForeground(Color.WHITE);
        questionList.setFixedCellHeight(28);

        JButton previousButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton saveCloseButton = new JButton("Save & Close");
        JButton submitButton = new JButton("Submit Exam");

        Runnable persistCurrentAnswer = () -> {
            Question currentQuestion = questions.get(currentIndex[0]);
            String selectedAnswer = getCurrentAnswer(currentQuestion, optionButtons, textAnswerArea);
            if (selectedAnswer.isBlank()) {
                workingAnswers.remove(currentQuestion.getQuestionId());
            } else {
                workingAnswers.put(currentQuestion.getQuestionId(), selectedAnswer);
            }
            saveAttemptProgress(studentId, attempt.studentName, exam, currentIndex[0], attempt.startedAt, workingAnswers);
            refreshQuestionList(questionListModel, questions, workingAnswers, currentIndex[0]);
        };

        Runnable refreshView = () -> {
            Question currentQuestion = questions.get(currentIndex[0]);
            headerLabel.setText("Question " + (currentIndex[0] + 1) + " of " + questions.size()
                + "   |   Answered: " + countAnsweredQuestions(questions, workingAnswers)
                + "   |   Type: " + currentQuestion.getQuestionType());
            questionArea.setText(currentQuestion.getQuestionText() + "\n\nMarks: " + currentQuestion.getMarks());
            questionArea.setCaretPosition(0);

            if (currentQuestion.isTextBased()) {
                textAnswerArea.setText(workingAnswers.getOrDefault(currentQuestion.getQuestionId(), ""));
                answerCardLayout.show(answerCardPanel, "TEXT");
            } else {
                List<String> options = currentQuestion.getAllOptions();
                for (int i = 0; i < optionButtons.length; i++) {
                    boolean visible = i < options.size();
                    optionButtons[i].setVisible(visible);
                    if (visible) {
                        char optionLetter = (char) ('A' + i);
                        optionButtons[i].setText(optionLetter + ") " + options.get(i));
                        optionButtons[i].setActionCommand(String.valueOf(optionLetter));
                    } else {
                        optionButtons[i].setText("");
                        optionButtons[i].setActionCommand("");
                    }
                }
                applySavedAnswer(workingAnswers.get(currentQuestion.getQuestionId()), optionButtons, answerGroup);
                answerCardLayout.show(answerCardPanel, "MCQ");
            }

            previousButton.setEnabled(currentIndex[0] > 0);
            nextButton.setEnabled(currentIndex[0] < questions.size() - 1);
            refreshQuestionList(questionListModel, questions, workingAnswers, currentIndex[0]);
            if (questionList.getSelectedIndex() != currentIndex[0]) {
                questionList.setSelectedIndex(currentIndex[0]);
            }
        };

        long[] remainingSeconds = {getRemainingSeconds(exam, attempt.startedAt)};
        Runnable updateTimerLabel = () -> {
            timerLabel.setText("Time Left: " + formatRemainingTime(remainingSeconds[0]));
            timerLabel.setForeground(remainingSeconds[0] <= 60 ? new Color(255, 193, 7) : new Color(52, 199, 123));
        };
        updateTimerLabel.run();

        final javax.swing.Timer countdownTimer = new javax.swing.Timer(1000, event -> {
            remainingSeconds[0] = getRemainingSeconds(exam, attempt.startedAt);
            updateTimerLabel.run();
            if (remainingSeconds[0] <= 0) {
                ((javax.swing.Timer) event.getSource()).stop();
                persistCurrentAnswer.run();
                submitted[0] = true;
                JOptionPane.showMessageDialog(dialog,
                    "Time is up. The exam will now be submitted automatically.",
                    "Time Up",
                    JOptionPane.WARNING_MESSAGE);
                dialog.dispose();
            }
        });
        countdownTimer.setInitialDelay(0);
        countdownTimer.start();

        for (JRadioButton optionButton : optionButtons) {
            optionButton.addActionListener(event -> persistCurrentAnswer.run());
        }

        questionList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int selected = questionList.getSelectedIndex();
            if (selected >= 0 && selected != currentIndex[0]) {
                persistCurrentAnswer.run();
                currentIndex[0] = selected;
                refreshView.run();
            }
        });

        previousButton.addActionListener(event -> {
            if (currentIndex[0] > 0) {
                persistCurrentAnswer.run();
                currentIndex[0]--;
                refreshView.run();
            }
        });

        nextButton.addActionListener(event -> {
            if (currentIndex[0] < questions.size() - 1) {
                persistCurrentAnswer.run();
                currentIndex[0]++;
                refreshView.run();
            }
        });

        saveCloseButton.addActionListener(event -> {
            persistCurrentAnswer.run();
            countdownTimer.stop();
            dialog.dispose();
        });

        submitButton.addActionListener(event -> {
            persistCurrentAnswer.run();
            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Submit this exam now?",
                "Confirm Submission",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                submitted[0] = true;
                countdownTimer.stop();
                dialog.dispose();
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                persistCurrentAnswer.run();
                countdownTimer.stop();
                dialog.dispose();
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(topInfoPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(questionArea), BorderLayout.CENTER);
        centerPanel.add(answerCardPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(260, 0));

        JLabel jumpLabel = new JLabel("Jump to question");
        jumpLabel.setForeground(Color.WHITE);
        jumpLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rightPanel.add(jumpLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(questionList), BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footerPanel.setOpaque(false);
        footerPanel.add(previousButton);
        footerPanel.add(nextButton);
        footerPanel.add(saveCloseButton);
        footerPanel.add(submitButton);

        root.add(centerPanel, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.EAST);
        root.add(footerPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        refreshView.run();
        dialog.setVisible(true);

        if (!submitted[0]) {
            return null;
        }
        return new DialogAttemptResult(attempt.studentName, attempt.startedAt, workingAnswers, true);
    }

    private void refreshQuestionList(DefaultListModel<String> model, List<Question> questions,
                                     Map<Integer, String> answers, int currentIndex) {
        model.clear();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String marker = answers.containsKey(question.getQuestionId()) ? "✓" : "○";
            String prefix = i == currentIndex ? "➤ " : "  ";
            model.addElement(prefix + "Q" + (i + 1) + "  " + marker + "  "
                + shortenText(question.getQuestionText(), 28));
        }
    }

    private void applySavedAnswer(String answer, JRadioButton[] optionButtons, ButtonGroup answerGroup) {
        answerGroup.clearSelection();
        if (answer == null || answer.isBlank()) {
            return;
        }

        String normalized = answer.trim().toUpperCase();
        for (JRadioButton optionButton : optionButtons) {
            if (normalized.equals(optionButton.getActionCommand())) {
                optionButton.setSelected(true);
                return;
            }
        }
    }

    private String getCurrentAnswer(Question question, JRadioButton[] optionButtons, JTextArea textAnswerArea) {
        if (question.isTextBased()) {
            return textAnswerArea.getText().trim();
        }
        for (JRadioButton optionButton : optionButtons) {
            if (optionButton.isSelected()) {
                return optionButton.getActionCommand();
            }
        }
        return "";
    }

    private int countAnsweredQuestions(List<Question> questions, Map<Integer, String> answers) {
        int count = 0;
        for (Question question : questions) {
            String value = answers.get(question.getQuestionId());
            if (value != null && !value.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private String shortenText(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        if (compact.length() <= maxLength) {
            return compact;
        }
        return compact.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private int calculateScore(Exam exam, Map<Integer, String> answers) {
        int score = 0;
        for (Question question : exam.getQuestions()) {
            String answer = answers.getOrDefault(question.getQuestionId(), "");
            if (question.isCorrect(answer)) {
                score += question.getMarks();
            }
        }
        return score;
    }

    private void displayQuestion(Question question) {
        System.out.println();
        System.out.println("Q" + question.getQuestionId() + " [" + question.getQuestionType() + "]: " + question.getQuestionText());
        if (question.isTextBased()) {
            System.out.println("Type your answer below.");
            return;
        }

        List<String> options = question.getAllOptions();
        char option = 'A';
        for (String value : options) {
            System.out.println(option + ") " + value);
            option++;
        }
    }

    private long getRemainingSeconds(Exam exam, long startedAt) {
        long totalSeconds = Math.max(1, exam.getDurationMinutes()) * 60L;
        long elapsedSeconds = Math.max(0, (System.currentTimeMillis() - startedAt) / 1000L);
        return Math.max(0, totalSeconds - elapsedSeconds);
    }

    private String formatRemainingTime(long remainingSeconds) {
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void printSubmissionSummary(Result result) {
        System.out.println("\n===============================");
        System.out.println("Exam Submitted Successfully");
        System.out.println("===============================");
        System.out.println("Score      : " + result.getScore() + "/" + result.getTotalMarks());
        System.out.println("Percentage : " + String.format("%.1f%%", result.getPercentage()));
        System.out.println("Grade      : " + result.getGrade());
    }

    private void showReattemptDenied(Component parent, Exam exam) {
        JOptionPane.showMessageDialog(
            parent,
            REATTEMPT_NOT_ALLOWED_MESSAGE + "\nExam: " + exam.getTitle(),
            "Attempt Locked",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void printReattemptDenied(Exam exam) {
        System.out.println(REATTEMPT_NOT_ALLOWED_MESSAGE);
        System.out.println("Exam: " + exam.getTitle());
    }

    private static final class AttemptContext {
        private final String studentName;
        private final long startedAt;
        private final int startIndex;
        private final Map<Integer, String> answers;

        private AttemptContext(String studentName, long startedAt, int startIndex, Map<Integer, String> answers) {
            this.studentName = studentName;
            this.startedAt = startedAt;
            this.startIndex = startIndex;
            this.answers = answers;
        }
    }

    private static final class DialogAttemptResult {
        private final String studentName;
        private final long startedAt;
        private final Map<Integer, String> answers;
        private final boolean submitted;

        private DialogAttemptResult(String studentName, long startedAt, Map<Integer, String> answers, boolean submitted) {
            this.studentName = studentName;
            this.startedAt = startedAt;
            this.answers = new LinkedHashMap<>(answers);
            this.submitted = submitted;
        }
    }
}
