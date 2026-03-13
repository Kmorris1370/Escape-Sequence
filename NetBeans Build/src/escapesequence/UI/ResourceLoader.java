package escapesequence.UI;

/**
 * @authors Akera Griffith & Kaitlyn Morris
 * Class that loads assets
 */

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;              
import java.io.InputStream;
import javax.swing.ImageIcon;
public class ResourceLoader {


    // -------------------------------------------------------------------------
    // FONTS
    // -------------------------------------------------------------------------

    /**
     * Loads and registers a font from the classpath.
     * @param path  classpath path e.g. "/assets/font/vt323Font.ttf"
     * @param size  desired font size in points
     * @return      the loaded Font, or a fallback Arial font if loading fails
     */
    public static Font loadFont(String path, float size) {
        try {
            InputStream stream = ResourceLoader.class.getResourceAsStream(path);
            if (stream == null) {
                System.err.println("ResourceLoader: Font not found at " + path);
                return fallbackFont(size);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            return font;
        } catch (Exception e) {
            System.err.println("ResourceLoader: Failed to load font at " + path);
            e.printStackTrace();
            return fallbackFont(size);
        }
    }

    /**
     * Fallback font in case the custom font fails to load.
     */
    private static Font fallbackFont(float size) {
        return new Font("Arial", Font.PLAIN, (int) size);
    }

    // -------------------------------------------------------------------------
    // IMAGES
    // -------------------------------------------------------------------------

    /**
     * Loads and scales an image to the specified dimensions.
     * Best used for background images that need to fit a panel or screen.
     * @param path      classpath path e.g. "/assets/pictures/background.png"
     * @param width     desired width in pixels
     * @param height    desired height in pixels
     * @return          scaled ImageIcon, or null if loading fails
     */
    public static ImageIcon loadImageScaled(String path, int width, int height) {
        try {
            java.net.URL imgURL = ResourceLoader.class.getResource(path);
            if (imgURL == null) {
                System.err.println("ResourceLoader: Image not found at " + path);
                return null;
            }
            ImageIcon icon = new ImageIcon(imgURL);
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("ResourceLoader: Failed to load image at " + path);
            e.printStackTrace();
            return null;
        }
    }
}
