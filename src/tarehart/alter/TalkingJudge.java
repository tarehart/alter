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
    private boolean mutedManually;

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
                if (!isManuallyMuted()) {
                    SoundPlayer.boop();
                }
            }
        });
        sneezeTimer.setRepeats(false);
    }

    public void gainSound() {

        if (mutedManually) {
            return;
        }

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
            engageMute();
            mutedForSneeze = true;
            sneezeTimer.start();
        }
    }

    public void toggleManualMute() {
        if (!mutedManually) {
            mutedManually = true;
            mutedForSneeze = false;
            engageMute();
            sneezeTimer.stop();
        } else {
            mutedManually = false;
            if (!isMutedForSneeze()) {
                SoundPlayer.boop();
            }
        }
    }

    private void engageMute() {
        presser.release();
        SoundPlayer.beepBeep();
    }

    public boolean isManuallyMuted() {
        return mutedManually;
    }

    public boolean isMutedForSneeze() {
        return mutedForSneeze;
    }
}
