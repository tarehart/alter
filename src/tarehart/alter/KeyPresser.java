package tarehart.alter;

import java.awt.*;

public class KeyPresser {

    private Robot robot;
    private int key;
    private boolean isKeyDown;


    public KeyPresser() throws AWTException {
        this.robot = new Robot();
    }

    public void setKey(int key) {
        boolean wasDown = false;

        if (isKeyDown) {
            wasDown = true;
            release();
        }

        this.key = key;

        if (wasDown) {
            beginHold();
        }
    }

    public int getKey() {
        return key;
    }

    public boolean beginHold() {
        if (!isKeyDown) {
            try {
                robot.keyPress(key);
                isKeyDown = true;
                return true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean release() {

        if (isKeyDown) {

            try {
                robot.keyRelease(key);
                isKeyDown = false;
                return true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean isPressing() {
        return isKeyDown;
    }
}
