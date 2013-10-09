package tarehart.alter;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;
import java.util.prefs.Preferences;


public class AlterForm {

    private static final int MAX_VOLUME = 100;
    private static final int LINGER_PERIOD = 800; // 1000 milliseconds = 1 second
    private static final int DEFAULT_THRESHOLD = 50;
    private static final String CONFIG_THRESHOLD = "threshold";
    private static final String CONFIG_KEY_CODE = "keyCode";
    private static final String CONFIG_MIC_CHOICE = "microphoneChoice";
    private static final int SNEEZE_PERIOD = 5000; // 5000 milliseconds = 5 seconds
    private static final String CONFIG_MUTE_CODE = "sneezeCode";
    private TalkingJudge judge;
    private KeyPresser presser;
    private MicrophoneAnalyzer microphoneAnalyzer;
    protected Preferences preferences = Preferences.userNodeForPackage(AlterForm.class);

    public AlterForm() throws AWTException {

        populateMicrophoneCombo();

        microphoneAnalyzer = new MicrophoneAnalyzer();
        presser = new KeyPresser();
        judge = new TalkingJudge(presser, LINGER_PERIOD, SNEEZE_PERIOD);

        progressBar1.setMaximum(MAX_VOLUME);
        slider1.setMaximum(MAX_VOLUME);

        setUIFromPreferences();

        setMicrophoneFromUI();
        setKeyFromUI();

        addUIListeners();
        setSneezeHook();

        microphoneAnalyzer.addListener(new AmplitudeUpdateListener() {
            @Override
            public void amplitudeUpdated(float newAmplitude) {
                int level = (int) (newAmplitude * 10); // multiply by 10 so we get finer config with an int
                progressBar1.setValue(level);
                if (level >= slider1.getValue()) {
                    judge.gainSound();
                    pressingStatus.setBackground(Color.green);
                } else {
                    judge.loseSound();
                }

                if (!judge.hearsTalking()) {
                    pressingStatus.setBackground(Color.darkGray);
                }

                if (!judge.isMuted()) {
                    muteStatus.setBackground(Color.darkGray);
                }
            }
        });

    }

    private boolean setSneezeHook() {
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the sneeze hook.");
            System.err.println(ex.getMessage());

            return false;
        }

        //Construct the example object and initialze native hook.
        GlobalScreen.getInstance().addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                if (nativeKeyEvent.getKeyCode() == (Integer) spinner2.getValue()) {
                    judge.sneezeIncoming();
                    muteStatus.setBackground(Color.green);
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }
        });

        return true;
    }

    private void addUIListeners() {
        spinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setKeyFromUI();
            }
        });

        comboBox1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setMicrophoneFromUI();
                }
            }
        });

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyGrabber.grabNextKey(spinner1);
            }
        });

        sneezeSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyGrabber.grabNextKey(spinner2);
            }
        });
    }

    private void setKeyFromUI() {
        presser.setKey((Integer) spinner1.getValue());
    }

    private void setMicrophoneFromUI() {
        try {
            microphoneAnalyzer.setMixer((Mixer.Info) comboBox1.getSelectedItem());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void setUIFromPreferences() {

        slider1.setValue(preferences.getInt(CONFIG_THRESHOLD, DEFAULT_THRESHOLD));
        spinner1.setValue(preferences.getInt(CONFIG_KEY_CODE, KeyEvent.VK_ALT));
        spinner2.setValue(preferences.getInt(CONFIG_MUTE_CODE, KeyEvent.VK_F5));
        comboBox1.setSelectedIndex(preferences.getInt(CONFIG_MIC_CHOICE, 0));

    }

    private void savePreferences() {
        preferences.putInt(CONFIG_KEY_CODE, (Integer) spinner1.getValue());
        preferences.putInt(CONFIG_MUTE_CODE, (Integer) spinner2.getValue());
        preferences.putInt(CONFIG_THRESHOLD, slider1.getValue());
        preferences.putInt(CONFIG_MIC_CHOICE, comboBox1.getSelectedIndex());
    }

    private void populateMicrophoneCombo() {

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

    }


    private WindowListener getWindowListener() {
        return new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) { }

            @Override
            public void windowClosing(WindowEvent e) {

                microphoneAnalyzer.stop();
                presser.release();

                savePreferences();
            }

            @Override
            public void windowClosed(WindowEvent e) { }

            @Override
            public void windowIconified(WindowEvent e) { }

            @Override
            public void windowDeiconified(WindowEvent e) { }

            @Override
            public void windowActivated(WindowEvent e) { }

            @Override
            public void windowDeactivated(WindowEvent e) { }
        };
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
            frame.addWindowListener(m.getWindowListener());
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    private JProgressBar progressBar1;
    private JSlider slider1;
    private JPanel rootPanel;
    private JButton button1;
    private JSpinner spinner1;
    private JPanel pressingStatus;
    private JComboBox<Mixer.Info> comboBox1;
    private JTextPane thisAppWillTakeTextPane;
    private JButton sneezeSelect;
    private JSpinner spinner2;
    private JPanel muteStatus;
}
