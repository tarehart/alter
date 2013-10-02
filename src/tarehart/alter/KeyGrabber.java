package tarehart.alter;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyGrabber {

    public static void grabNextKey(final JSpinner listenMe) {

        listenMe.grabFocus();

        listenMe.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                listenMe.setValue(keyCode);
                listenMe.removeKeyListener(this);
            }

            @Override
            public void keyReleased(KeyEvent e) { }
        });
    }

}
