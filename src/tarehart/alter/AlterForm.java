package tarehart.alter;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;


public class AlterForm {

    public static final int MAX_VOLUME = 50;
    public static final int GRACE_PERIOD = 1000; // 1000 milliseconds = 1 second
    private TalkingJudge judge;
    private KeyPresser presser;
    private MicrophoneAnalyzer microphoneAnalyzer;

    public AlterForm() throws AWTException {

        microphoneAnalyzer = new MicrophoneAnalyzer();
        setupMicrophones();

        presser = new KeyPresser();
        judge = new TalkingJudge(presser, GRACE_PERIOD);
        presser.setKey(KeyEvent.VK_ALT);
        spinner1.setValue(KeyEvent.VK_ALT);

        setupKeyBinder();

        progressBar1.setMaximum(MAX_VOLUME);
        slider1.setMaximum(MAX_VOLUME);

        slider1.setValue(3);

        microphoneAnalyzer.addListener(new AmplitudeUpdateListener() {
            @Override
            public void amplitudeUpdated(float newAmplitude) {
                int level = (int) newAmplitude;
                progressBar1.setValue(level);
                if (level >= slider1.getValue()) {
                    judge.gainSound();
                    statusLight.setBackground(Color.green);
                } else {
                    judge.loseSound();
                }

                if (!judge.hearsTalking()) {
                    statusLight.setBackground(Color.darkGray);
                }
            }
        });


        spinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                presser.setKey((Integer) spinner1.getValue());
            }
        });

    }

    private void setupKeyBinder() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyGrabber.grabNextKey(spinner1);
            }
        });
    }

    private void setupMicrophones() {

        comboBox1.setRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                DefaultListCellRenderer renderer = new DefaultListCellRenderer();
                Mixer.Info mixer = (Mixer.Info) value;
                return renderer.getListCellRendererComponent(list, mixer.getName(), index, isSelected, cellHasFocus);
            }
        });

        List<Mixer.Info> mixers = AudioSystemHelper.ListAudioInputDevices();
        for (Mixer.Info mixer: mixers) {
            comboBox1.addItem(mixer);
        }

        try {
            microphoneAnalyzer.setMixer((Mixer.Info) comboBox1.getItemAt(0));
        } catch (LineUnavailableException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        comboBox1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Mixer.Info mixer = (Mixer.Info) e.getItem();
                    try {
                        microphoneAnalyzer.setMixer(mixer);
                    } catch (LineUnavailableException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Essen
        }
        JFrame frame = new JFrame("Alt'er");
        URL url = ClassLoader.getSystemResource("tarehart/alter/resources/alter.png");
        Image img = Toolkit.getDefaultToolkit().createImage(url);
        frame.setIconImage(img);

        try {
            AlterForm m = new AlterForm();
            frame.setContentPane(m.rootPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (AWTException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private JProgressBar progressBar1;
    private JSlider slider1;
    private JPanel rootPanel;
    private JButton button1;
    private JSpinner spinner1;
    private JPanel statusLight;
    private JComboBox comboBox1;
    private JTextPane thisAppWillTakeTextPane;
}
