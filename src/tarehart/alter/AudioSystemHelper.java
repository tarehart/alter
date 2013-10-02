package tarehart.alter;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;

/**
 * Some code borrowed from this tutorial:
 * http://www.technogumbo.com/tutorials/Java-Microphone-Selection-And-Level-Monitoring/Java-Microphone-Selection-And-Level-Monitoring.php
 */
public class AudioSystemHelper {

    public static final Line.Info targetDLInfo = new Line.Info(TargetDataLine.class);

    public static List<Mixer.Info> ListAudioInputDevices() {
        List<Mixer.Info> returnList = new ArrayList<Mixer.Info>();
        Mixer.Info[] mixerInfo;

        mixerInfo = AudioSystem.getMixerInfo();

        for(int i = 0; i < mixerInfo.length; i++) {
            Mixer currentMixer = AudioSystem.getMixer(mixerInfo[i]);

            if( currentMixer.isLineSupported(targetDLInfo) ) {
                returnList.add( mixerInfo[i] );
            }
        }

        return returnList;
    }
}
