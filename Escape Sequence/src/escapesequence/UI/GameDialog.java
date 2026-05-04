package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Reusable themed dialog utility for all in-game messages
 */

import java.awt.*;
import javax.swing.*;

public class GameDialog {

    // ── Confirmation Dialog ──────────────────────────────────

    // Shows a Yes/No dialog and returns true if user clicks Yes
    // Example: if (GameDialog.confirm(this, "Are you sure you want to quit?")) { ... }
    public static boolean confirm(Frame parent, String message) {
        final boolean[] result = {false}; // array used so lambda can modify the value

        JDialog dialog = new JDialog(parent, true); // true = modal (blocks until closed)
        dialog.setUndecorated(true);

        // Message
        JLabel msg = buildMessageLabel(message, 24f);

        // Yes button — sets result to true then closes
        JButton yes = buildButton("Yes");
        yes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        // No button — closes without changing result
        JButton no = buildButton("No");
        no.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(10, 10, 15));
        buttonPanel.add(yes);
        buttonPanel.add(no);

        JPanel panel = buildPanel();
        panel.add(msg, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        showDialog(dialog, panel, parent);
        return result[0];
    }

    // ── Timed Auto-Close Dialog ──────────────────────────────

    // Shows a message that automatically disappears after durationMs milliseconds
    // Non-blocking — game continues while message is visible
    // Example: GameDialog.showTimed(this, "Player 2's turn!", 1500);
    public static void showTimed(Frame parent, String message, int durationMs) {
        JDialog dialog = new JDialog(parent, false); // false = non-modal (non-blocking)
        dialog.setUndecorated(true);

        JLabel msg = buildMessageLabel(message, 28f);

        JPanel panel = buildPanel();
        panel.add(msg, BorderLayout.CENTER);

        showDialog(dialog, panel, parent);

        // Auto-close after specified duration
        javax.swing.Timer timer = new javax.swing.Timer(durationMs, e -> dialog.dispose());
        timer.setRepeats(false); // fire once only
        timer.start();
    }

    // ── Standard OK Dialog ───────────────────────────────────

    // Shows a message with an OK button — blocks until dismissed
    // Example: GameDialog.show(this, "You advance — no keycard.");
    public static void show(Frame parent, String message) {
        JDialog dialog = new JDialog(parent, true); // true = modal (blocks)
        dialog.setUndecorated(true);

        JLabel msg = buildMessageLabel(message, 28f);
        msg.setHorizontalAlignment(SwingConstants.CENTER); // center-align all dialog text

        JButton ok = buildButton("OK");
        ok.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(10, 10, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 15, 30));
        buttonPanel.add(ok);

        JPanel panel = buildPanel();
        panel.add(msg, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        showDialog(dialog, panel, parent);
    }

    // ── Input Dialog ─────────────────────────────────────────

    // Shows a styled text-field dialog. Returns the entered string, or null if cancelled.
    // Example: String val = GameDialog.showInput(this, "Choose a value from 1 to 9 for Wild.");
    public static String showInput(Frame parent, String prompt) {
        final String[] result = {null};

        JDialog dialog = new JDialog(parent, true);
        dialog.setUndecorated(true);

        JLabel msg = buildMessageLabel(prompt, 24f);

        JTextField field = new JTextField(10);
        field.setFont(FontLoader.getVT323(24f));
        field.setForeground(new Color(198, 40, 40));
        field.setBackground(new Color(10, 10, 15));
        field.setCaretColor(new Color(198, 40, 40));
        field.setBorder(BorderFactory.createLineBorder(new Color(198, 40, 40)));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.addActionListener(e -> { result[0] = field.getText(); dialog.dispose(); }); // Enter key

        JPanel fieldPanel = new JPanel();
        fieldPanel.setBackground(new Color(10, 10, 15));
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 10, 30));
        fieldPanel.add(field);

        JButton ok = buildButton("OK");
        ok.addActionListener(e -> { result[0] = field.getText(); dialog.dispose(); });

        JButton cancel = buildButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(10, 10, 15));
        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        JPanel panel = buildPanel();
        panel.add(msg, BorderLayout.NORTH);
        panel.add(fieldPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        showDialog(dialog, panel, parent);
        return result[0];
    }

    // ── Choice Dialog ─────────────────────────────────────────

    // Shows a styled dropdown dialog. Returns the selected option, or null if cancelled.
    // Example: String pick = GameDialog.showChoice(this, "Choose a target to freeze:", names);
    public static String showChoice(Frame parent, String prompt, String[] options) {
        final String[] result = {null};

        JDialog dialog = new JDialog(parent, true);
        dialog.setUndecorated(true);

        JLabel msg = buildMessageLabel(prompt, 24f);

        final Color BG = new Color(10, 10, 15);
        final Color RED = new Color(198, 40, 40);

        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(FontLoader.getVT323(22f));
        combo.setForeground(RED);
        combo.setBackground(BG);
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createLineBorder(RED, 1));
        combo.setOpaque(true);

        // Replace the default UI button/border so no white frame is drawn
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected javax.swing.JButton createArrowButton() {
                javax.swing.JButton b = new javax.swing.plaf.basic.BasicArrowButton(
                        javax.swing.plaf.basic.BasicArrowButton.SOUTH,
                        BG, BG, RED, BG);
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                return b;
            }
        });

        // Style the editor field (the visible selected value)
        Object editorComp = combo.getEditor().getEditorComponent();
        if (editorComp instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) editorComp).setBackground(BG);
            ((javax.swing.JComponent) editorComp).setForeground(RED);
            ((javax.swing.JComponent) editorComp).setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        }

        // Render dropdown items with dark bg + red text; highlighted item uses dark red, no white
        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setFont(FontLoader.getVT323(22f));
                l.setForeground(RED);
                l.setBackground(isSelected ? new Color(60, 10, 10) : BG);
                l.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                return l;
            }
        });

        JPanel comboPanel = new JPanel();
        comboPanel.setBackground(new Color(10, 10, 15));
        comboPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 10, 30));
        comboPanel.add(combo);

        JButton ok = buildButton("OK");
        ok.addActionListener(e -> { result[0] = (String) combo.getSelectedItem(); dialog.dispose(); });

        JButton cancel = buildButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(10, 10, 15));
        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        JPanel panel = buildPanel();
        panel.add(msg, BorderLayout.NORTH);
        panel.add(comboPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        showDialog(dialog, panel, parent);
        return result[0];
    }

    // ── Shared Helpers ───────────────────────────────────────

    // Builds a styled message label with the game's font and color
    private static JLabel buildMessageLabel(String message, float fontSize) {
        JLabel label = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        label.setFont(FontLoader.getVT323(fontSize));
        label.setForeground(new Color(198, 40, 40));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        return label;
    }

    // Builds a styled button with the game's font and a wider uniform border
    private static JButton buildButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FontLoader.getVT323(24f));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(10, 10, 15));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setOpaque(false);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // bypass L&F pressed/armed fill
        btn.setPreferredSize(new java.awt.Dimension(100, 36));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(198, 40, 40), 2),
            BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        return btn;
    }

    // Builds the dark themed panel that wraps all dialog content
    private static JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(10, 10, 15));
        panel.setBorder(BorderFactory.createLineBorder(new Color(198, 40, 40), 2));
        return panel;
    }

    // Adds content to dialog, packs, centers, and shows it
    private static void showDialog(JDialog dialog, JPanel panel, Frame parent) {
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent); // centers over parent frame
        dialog.setVisible(true);
    }
}