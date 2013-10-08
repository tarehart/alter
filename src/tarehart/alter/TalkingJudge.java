package tarehart.alter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TalkingJudge {

    private int lingerTime;
    private int sneezeTime;
    private KeyPresser presser;
    private Timer lingerTimer;
    private Timer sneezeTimer;
    private boolean mutedForSneeze;

    public TalkingJudge(KeyPresser presser, int lingerTime, int sneezeTime) {
        this.lingerTime = lingerTime;
        this.presser = presser;
        this.sneezeTime = sneezeTime;

        setupTimers();

    }

    private void setupTimers() {
        lingerTimer = new Timer(lingerTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                presser.release();
            }
        });
        lingerTimer.setRepeats(false);

        sneezeTimer = new Timer(sneezeTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mutedForSneeze = false;
            }
        });
        sneezeTimer.setRepeats(false);
    }

    public void gainSound() {

        if (mutedForSneeze) {
            sneezeTimer.restart();
        } else {
            if (lingerTimer.isRunning()) {
                lingerTimer.stop();
            } else {
                presser.beginHold();
            }
        }
    }

    public void loseSound() {

        if (!hearsTalking()) {
            return;
        }

        if (!lingerTimer.isRunning()) {
            lingerTimer.restart();
        }
    }

    public boolean hearsTalking() {
        return presser.isPressing();
    }

    public void sneezeIncoming() {
        if (sneezeTimer.isRunning()) {
            sneezeTimer.restart();
        } else {
            presser.release();
            mutedForSneeze = true;
            sneezeTimer.start();
        }
    }

    public boolean isMuted() {
        return mutedForSneeze;
    }
}
