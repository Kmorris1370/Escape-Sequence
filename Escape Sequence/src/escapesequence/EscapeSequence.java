package escapesequence;

import escapesequence.UI.*;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import java.awt.Color;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Escape Sequence Main Class
 */
public class EscapeSequence {
    public static void main(String[] args) {

        // Apply themed tooltip styling globally
        UIManager.put("ToolTip.background",  new Color(10, 10, 15));
        UIManager.put("ToolTip.foreground",  new Color(198, 40, 40));
        UIManager.put("ToolTip.border",
            BorderFactory.createLineBorder(new Color(198, 40, 40), 1));
        // Font is applied via L&F — VT323 at 18pt
        try {
            UIManager.put("ToolTip.font",
                escapesequence.UI.FontLoader.getVT323(18f));
        } catch (Exception ignored) {}

        //Start Game
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainMenu mainMenu = new MainMenu();
            mainMenu.setVisible(true);
        });
    }
}