package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Reusable themed dialog utility for all in-game messages
 * All dialogs are undecorated (no title bar) and styled to match the game theme
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
        msg.setHorizontalAlignment(SwingConstants.LEFT); // left-align for multi-line messages

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

    // Builds a styled button with the game's font and border
    private static JButton buildButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FontLoader.getVT323(24f));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(10, 10, 15));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(198, 40, 40)));
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