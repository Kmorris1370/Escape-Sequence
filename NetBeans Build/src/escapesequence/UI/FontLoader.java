package escapesequence.UI;

/**
 * @authors Akera Griffith & Kaitlyn Morris
 */

import java.awt.Font;

/**
 * Shortcut wrapper for loading the VT323 font at commonly used sizes.
 * @param size  desired font size in points e.g. 18f, 24f, 48f, 72f
 * @return      VT323 font at the specified size, or Arial if loading fails
 */

public class FontLoader {
    public static Font getVT323(float size) {
        return ResourceLoader.loadFont("/assets/font/vt323Font.ttf", size);
    }

}
