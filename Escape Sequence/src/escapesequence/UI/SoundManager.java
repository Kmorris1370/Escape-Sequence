package escapesequence.UI;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.AbstractButton;

public final class SoundManager {
    private SoundManager() {}

    public static void enableButtonSounds(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton) {
                ((AbstractButton) component).addActionListener(e -> playClick());
            }
            if (component instanceof Container) {
                enableButtonSounds((Container) component);
            }
        }
    }

    public static void playClick() {
        play("/assets/sounds/click.wav");
    }

    public static void playDeal() {
        play("/assets/sounds/deal.wav");
    }

    public static void playOutcome(boolean positive) {
        play(positive ? "/assets/sounds/win.wav" : "/assets/sounds/lose.wav");
    }

    private static void play(String path) {
        new Thread(() -> {
            try (InputStream raw = SoundManager.class.getResourceAsStream(path)) {
                if (raw == null) {
                    System.err.println("SoundManager: sound not found — " + path);
                    return;
                }
                try (BufferedInputStream buffered = new BufferedInputStream(raw);
                     AudioInputStream audioStream = AudioSystem.getAudioInputStream(buffered)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.addLineListener(event -> {
                        LineEvent.Type type = event.getType();
                        if (type == LineEvent.Type.STOP || type == LineEvent.Type.CLOSE) {
                            clip.close();
                        }
                    });
                    clip.start();
                }
            } catch (Exception e) {
                System.err.println("SoundManager: failed to play — " + path + " (" + e.getMessage() + ")");
            }
        }, "escape-sequence-sound").start();
    }
}
