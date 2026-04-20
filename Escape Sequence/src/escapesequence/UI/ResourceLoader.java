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

    //Method to load the font
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
    
    //Fallback font incase the issue with loading the desired font
    private static Font fallbackFont(float size) {
        return new Font("Arial", Font.PLAIN, (int) size);
    }
    
    //Method to get the image url or the cards
    public static ImageIcon loadCardImage(int value) {
        return loadImageScaled("/assets/pictures/" + value + ".jpg", 50, 50);
    }

    // For backgrounds — stretches to fill exactly
    public static ImageIcon loadImageScaled(String path, int width, int height) {
        try {
            java.net.URL imgURL = ResourceLoader.class.getResource(path);
            if (imgURL == null) {
                System.err.println("[ResourceLoader] FAILED — image not found at: " + path);
                return null;
            }
            ImageIcon icon = new ImageIcon(imgURL);
            if (icon.getIconWidth() == -1) {
                System.err.println("[ResourceLoader] FAILED — image could not be read: " + path);
                return null;
            }
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("[ResourceLoader] EXCEPTION loading: " + path);
            e.printStackTrace();
            return null;
        }
    }

// For cards and icons — maintains aspect ratio
    public static ImageIcon loadImageFitted(String path, int maxWidth, int maxHeight) {
        try {
            java.net.URL imgURL = ResourceLoader.class.getResource(path);
            if (imgURL == null) {
                System.err.println("[ResourceLoader] FAILED — image not found at: " + path);
                return null;
            }
            ImageIcon icon = new ImageIcon(imgURL);
            if (icon.getIconWidth() == -1) {
                System.err.println("[ResourceLoader] FAILED — image could not be read: " + path);
                return null;
            }
            int origWidth = icon.getIconWidth();
            int origHeight = icon.getIconHeight();
            double scale = Math.min((double) maxWidth / origWidth, (double) maxHeight / origHeight);
            int newWidth = (int) (origWidth * scale);
            int newHeight = (int) (origHeight * scale);
            Image scaled = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("[ResourceLoader] EXCEPTION loading: " + path);
            e.printStackTrace();
            return null;
        }
    }
}
