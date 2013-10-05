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
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // no biggie
            }
        }
    }


    private float calculateRMSLevel(byte[] audioData) {
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

    public void stop() {
        killExistingThread();
    }


    class CaptureThread extends Thread{

        //An arbitrary-size temporary holding buffer
        byte tempBuffer[] = new byte[250];
        public void run(){

            threadEnded = false;
            stopCapture = false;
            try{
                while(!stopCapture) {
                    int bytesRead = microphone.read(tempBuffer, 0, tempBuffer.length);
                    if(bytesRead > 0){
                        float currentLevel = calculateRMSLevel(tempBuffer);
                        for (AmplitudeUpdateListener aul: listeners) {
                            aul.amplitudeUpdated(currentLevel);
                        }
                    }

                }

                microphone.close();
                threadEnded = true;
            } catch (Throwable e) {
                System.out.println(e);
                System.exit(-1);
            }
        }
    }
}
