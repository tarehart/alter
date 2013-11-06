package tarehart.alter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;

public class MicrophoneWorker extends SwingWorker {

    private final TargetDataLine microphone;

    public MicrophoneWorker(TargetDataLine microphone) {
        this.microphone = microphone;
    }

    //Small chunks for frequent amplitude updates
    byte tempBuffer[] = new byte[20];

    public static AudioFormat getAudioFormat() {
        // 1000 samples per second tends to exclude high-pitch noises like keyboard clacking
        // and bad attempts at whistling. Also keeps CPU utilization low.
        return new AudioFormat(1000, 8, 1, true, true);
    }

    @Override
    protected Object doInBackground() throws Exception {
        try {
            while (!isCancelled()) {
                int bytesRead = microphone.read(tempBuffer, 0, tempBuffer.length);
                if (bytesRead > 0) {
                    int currentLevel = (int) (calculateRMSLevel(tempBuffer) * 10);
                    if (currentLevel > 100) currentLevel = 100;
                    if (currentLevel < 0) currentLevel = 0;
                    setProgress(currentLevel);
                }

            }

            microphone.close();
        } catch (Throwable e) {
            System.out.println(e);
            System.exit(-1);
        }

        return null;
    }

    private static float calculateRMSLevel(byte[] audioData) {
        // audioData might be buffered data read from a data line
        long sum = 0;
        for (byte aud : audioData) {
            sum += aud;
        }

        double average = sum / audioData.length;

        double sumMeanSquare = 0;
        for (byte aud : audioData) {
            sumMeanSquare = sumMeanSquare + Math.pow(aud - average, 2);
        }

        double averageMeanSquare = sumMeanSquare / audioData.length;
        return (float)(Math.pow(averageMeanSquare, .5) + .5);
    }

}