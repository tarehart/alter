package tarehart.alter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DeadMicDetector {

    private Timer deathCount;
    private List<ActionListener> listeners = new ArrayList<ActionListener>();

    public DeadMicDetector() {
        deathCount = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ActionListener l: listeners) {
                    l.actionPerformed(e);
                }
            }
        });
        deathCount.setRepeats(false);
    }


    public void addEventListener(ActionListener listener)  {
        listeners.add(listener);
    }

    public void lookAlive() {
        deathCount.restart();
    }

}
