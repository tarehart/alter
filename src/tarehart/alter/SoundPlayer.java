package tarehart.alter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class SoundPlayer {

    private static synchronized void playSound(final String name) {
        new Runnable() {
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();

                    URL url = SoundPlayer.class.getResource("resources/" + name);
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.run();
    }

    public static void beepBeep() {
        playSound("beepbeep.wav");
    }

    public static void boop() {
        playSound("boop.wav");
    }

}
