package tarehart.alter;

import javax.sound.sampled.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Some code borrowed from this tutorial:
 * http://www.technogumbo.com/tutorials/Java-Microphone-Selection-And-Level-Monitoring/Java-Microphone-Selection-And-Level-Monitoring.php
 */
public class MicrophoneAnalyzer {

    private TargetDataLine microphone;

    private boolean stopCapture = false;
    private boolean threadEnded = true;

    private List<AmplitudeUpdateListener> listeners;

    public MicrophoneAnalyzer() {

        listeners = new LinkedList<AmplitudeUpdateListener>();

    }

    public void addListener(AmplitudeUpdateListener listener) {
        listeners.add(listener);
    }

    public void setMixer(Mixer.Info info) throws LineUnavailableException {

        killExistingThread();

        Mixer mixer = AudioSystem.getMixer(info);
        microphone = (TargetDataLine)mixer.getLine(AudioSystemHelper.targetDLInfo);

        AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, true);
        microphone.open(format);
        microphone.start();

        Thread captureThread = new CaptureThread();
        captureThread.start();
    }

    private void killExistingThread() {
        stopCapture = true;
        while (!threadEnded) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) { }
        }
    }


    private float calculateRMSLevel(byte[] audioData) {
        // audioData might be buffered data read from a data line
        long lSum = 0;
        for(int i = 0; i < audioData.length; i++) {
            lSum = lSum + audioData[i];
        }

        double dAvg = lSum / audioData.length;

        double sumMeanSquare = 0d;
        for(int j = 0; j < audioData.length; j++) {
            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);
        }

        double averageMeanSquare = sumMeanSquare / audioData.length;
        return (float)(Math.pow(averageMeanSquare, 0.5) + 0.5);
    }


    class CaptureThread extends Thread{

        //An arbitrary-size temporary holding buffer
        byte tempBuffer[] = new byte[100];
        public void run(){

            threadEnded = false;
            stopCapture = false;
            try{
                while(!stopCapture) {
                    int cnt = microphone.read(tempBuffer, 0, tempBuffer.length);
                    if(cnt > 0){
                        float currentLevel = calculateRMSLevel(tempBuffer);
                        for (AmplitudeUpdateListener aul: listeners) {
                            aul.amplitudeUpdated(currentLevel);
                        }
                    }
                }

                microphone.close();
                threadEnded = true;
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }
}
