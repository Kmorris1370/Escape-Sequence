package escape.sequence;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Escape Sequence Font Class
 */

import java.awt.Font;
import java.io.InputStream;

public class FontLoader {
    
    private static Font vt323Font;
    
    public static Font getVT323(float size) {
        if (vt323Font == null) {
            try {
                InputStream is = FontLoader.class.getResourceAsStream("/escape/Fonts/VT323-Regular.ttf");
                vt323Font = Font.createFont(Font.TRUETYPE_FONT, is);
            } catch (Exception e) {
                e.printStackTrace();
                vt323Font = new Font("Arial", Font.PLAIN, 24);
            }
        }
        return vt323Font.deriveFont(Font.PLAIN, size);
    }
}