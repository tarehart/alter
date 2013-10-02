package tarehart.alter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TalkingJudge {

    private int gracePeriod;
    private KeyPresser presser;
    private Timer timer;

    public TalkingJudge(KeyPresser presser, int gracePeriod) throws AWTException {
        this.gracePeriod = gracePeriod;
        this.presser = presser;

        setupTimer();

    }

    private void setupTimer() {
        timer = new Timer(gracePeriod, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                presser.release();
            }
        });

        timer.setRepeats(false);
    }

    public void gainSound() {
        if (timer.isRunning()) {
            timer.stop();
        } else {
            presser.beginHold();
        }
    }

    public void loseSound() {

        if (!hearsTalking()) {
            return;
        }

        if (!timer.isRunning()) {
            timer.restart();
        }
    }

    public boolean hearsTalking() {
        return presser.isPressing();
    }

}
