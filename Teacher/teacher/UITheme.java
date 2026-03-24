package teacher;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ExamGuard – teacher/UITheme.java
 *
 * Centralised styling constants and factory methods for the Teacher GUI.
 * All panels, dialogs, and tables use this so the look stays consistent.
 *
 * Team: TechXcoders | JAVA-IV-T223
 */
public final class UITheme {

    // ------------------------------------------------------------------ //
    //  Colour Palette
    // ------------------------------------------------------------------ //

    public static final Color BG_DARK        = new Color(18,  24,  38);   // window background
    public static final Color BG_PANEL       = new Color(28,  36,  54);   // card / panel background
    public static final Color BG_SIDEBAR     = new Color(22,  29,  46);   // left nav bar
    public static final Color ACCENT_BLUE    = new Color(66, 133, 244);   // primary action
    public static final Color ACCENT_GREEN   = new Color(52, 199, 123);   // success / active
    public static final Color ACCENT_RED     = new Color(234, 67,  53);   // danger / inactive
    public static final Color ACCENT_ORANGE  = new Color(251,188,  4);    // warning / edit
    public static final Color TEXT_PRIMARY   = new Color(232, 234, 240);  // headings
    public static final Color TEXT_SECONDARY = new Color(154, 160, 175);  // labels
    public static final Color BORDER_COLOR   = new Color(44,  53,  78);   // subtle borders
    public static final Color TABLE_HEADER   = new Color(36,  45,  66);
    public static final Color TABLE_ROW_EVEN = new Color(28,  36,  54);
    public static final Color TABLE_ROW_ODD  = new Color(32,  41,  62);
    public static final Color TABLE_SELECT   = new Color(66, 133, 244, 80);

    // ------------------------------------------------------------------ //
    //  Fonts
    // ------------------------------------------------------------------ //

    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD,  12);

    // ------------------------------------------------------------------ //
    //  Borders
    // ------------------------------------------------------------------ //

    public static final Border PADDING_10  = new EmptyBorder(10, 10, 10, 10);
    public static final Border PADDING_15  = new EmptyBorder(15, 15, 15, 15);
    public static final Border PADDING_20  = new EmptyBorder(20, 20, 20, 20);

    // No instantiation
    private UITheme() {}

    // ------------------------------------------------------------------ //
    //  Button Factory
    // ------------------------------------------------------------------ //

    /** Creates a styled rounded button with the given background colour. */
    public static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 36));
        return btn;
    }

    /** Creates a compact icon-style button (smaller width). */
    public static JButton makeSmallButton(String text, Color bg) {
        JButton btn = makeButton(text, bg);
        btn.setPreferredSize(new Dimension(110, 30));
        btn.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        return btn;
    }

    // ------------------------------------------------------------------ //
    //  Label Factory
    // ------------------------------------------------------------------ //

    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    public static JLabel makeTitle(String text) {
        return makeLabel(text, FONT_TITLE, TEXT_PRIMARY);
    }

    public static JLabel makeSubtitle(String text) {
        return makeLabel(text, FONT_SUBTITLE, TEXT_PRIMARY);
    }

    public static JLabel makeMuted(String text) {
        return makeLabel(text, FONT_SMALL, TEXT_SECONDARY);
    }

    // ------------------------------------------------------------------ //
    //  Input Factory
    // ------------------------------------------------------------------ //

    public static JTextField makeTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(BG_DARK);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static JSpinner makeIntSpinner(int min, int max, int value) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        sp.setFont(FONT_BODY);
        sp.setBackground(BG_DARK);
        sp.setForeground(TEXT_PRIMARY);
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BG_DARK);
            tf.setForeground(TEXT_PRIMARY);
            tf.setFont(FONT_BODY);
        }
        return sp;
    }

    public static JComboBox<String> makeComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_DARK);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_BODY);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return cb;
    }

    // ------------------------------------------------------------------ //
    //  Panel Factory
    // ------------------------------------------------------------------ //

    /** Opaque card panel with rounded visual appearance. */
    public static JPanel makeCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    // ------------------------------------------------------------------ //
    //  Table styling
    // ------------------------------------------------------------------ //

    public static void styleTable(JTable table) {
        table.setBackground(TABLE_ROW_EVEN);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(TABLE_SELECT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setFont(FONT_SUBTITLE);
        table.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }

    public static JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return sp;
    }

    // ------------------------------------------------------------------ //
    //  Status Badge
    // ------------------------------------------------------------------ //

    /** Returns coloured HTML text for use in a JLabel. */
    public static String badge(String text, boolean positive) {
        String color = positive ? "#34C77B" : "#EA4335";
        return "<html><span style='color:" + color + ";font-weight:bold'>" + text + "</span></html>";
    }
}
