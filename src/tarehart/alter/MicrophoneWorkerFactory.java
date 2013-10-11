package tarehart.alter;

import javax.sound.sampled.*;

/**
 * Some code borrowed from this tutorial:
 * http://www.technogumbo.com/tutorials/Java-Microphone-Selection-And-Level-Monitoring/Java-Microphone-Selection-And-Level-Monitoring.php
 */
public class MicrophoneWorkerFactory {

    private static TargetDataLine getLiveMicrophone(Mixer.Info info) throws LineUnavailableException {
        Mixer mixer = AudioSystem.getMixer(info);
        TargetDataLine microphone = (TargetDataLine)mixer.getLine(AudioSystemHelper.targetDLInfo);

        AudioFormat format = MicrophoneWorker.getAudioFormat();
        microphone.open(format);
        microphone.start();

        return microphone;
    }

    public static MicrophoneWorker createMicrophoneWorker(Mixer.Info info) throws LineUnavailableException {
        TargetDataLine mic = getLiveMicrophone(info);
        return new MicrophoneWorker(mic);
    }


}
