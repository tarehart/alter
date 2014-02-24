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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    private static final String CONFIG_SNEEZE_CODE = "sneezeCode";
    private static final String CONFIG_MUTE_CODE = "muteCode";
    public static final String CONFIG_SAMPLE_CODE = "sampleCode";
    public static final int DEFAULT_SAMPLE = 8;
    private TalkingJudge judge;
    private KeyPresser presser;
    protected Preferences preferences = Preferences.userNodeForPackage(AlterForm.class);
    private NativeKeyListener sneezeListener;
    private NativeKeyListener muteListener;
    private int noiseLevel;
    private SwingWorker microphoneWorker;

    public AlterForm() throws AWTException {

        populateMicrophoneCombo();

        presser = new KeyPresser();
        judge = new TalkingJudge(presser, LINGER_PERIOD, SNEEZE_PERIOD);

        progressBar1.setMaximum(MAX_VOLUME);
        slider1.setMaximum(MAX_VOLUME);

        setUIFromPreferences();

        try {
            setMicrophoneFromUI();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPanel, "This microphone isn't working at the moment. Picking a different one or restarting the program might work.");
        }

        setKeyFromUI();

        addUIListeners();
        setSneezeHook();

    }

    private void initMicrophoneWorker(Mixer.Info mixer, int sampleSize) throws LineUnavailableException, IllegalArgumentException {

        if (microphoneWorker != null) {
            microphoneWorker.cancel(true);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        microphoneWorker = MicrophoneWorkerFactory.createMicrophoneWorker(mixer, sampleSize);

        microphoneWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    noiseLevel = (Integer) evt.getNewValue();

                    if (noiseLevel < progressBar1.getMinimum()) noiseLevel = progressBar1.getMinimum();
                    if (noiseLevel > progressBar1.getMaximum()) noiseLevel = progressBar1.getMaximum();

                    progressBar1.setValue(noiseLevel);

                    if (noiseLevel >= slider1.getValue()) {
                        judge.gainSound();
                        pressingStatus.setBackground(Color.green);

                    } else {
                        judge.loseSound();
                    }

                    if (!judge.hearsTalking()) {
                        pressingStatus.setBackground(Color.darkGray);
                    }

                    if (!judge.isMutedForSneeze()) {
                        sneezeStatus.setBackground(Color.darkGray);
                    }
                }
            }
        });

        microphoneWorker.execute();
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

        sneezeListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                if (nativeKeyEvent.getKeyCode() == (Integer) spinner2.getValue()) {
                    judge.sneezeIncoming();
                    sneezeStatus.setBackground(Color.green);
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }
        };

        muteListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                if (nativeKeyEvent.getKeyCode() == (Integer) muteKeySpinner.getValue()) {
                    judge.toggleManualMute();
                    if (judge.isManuallyMuted()) {
                        muteStatus.setBackground(Color.green);
                    } else {
                        muteStatus.setBackground(Color.darkGray);
                    }
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }
        };

        GlobalScreen.getInstance().addNativeKeyListener(sneezeListener);

        GlobalScreen.getInstance().addNativeKeyListener(muteListener);

        return true;
    }

    private void releaseSneezeHook() {
        GlobalScreen.getInstance().removeNativeKeyListener(sneezeListener);
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
                    try {
                        setMicrophoneFromUI();
                    } catch (LineUnavailableException e1) {
                        e1.printStackTrace();
                    } catch (IllegalArgumentException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyGrabber.grabNextKey(spinner1);
            }
        });

        selectMuteKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyGrabber.grabNextKey(muteKeySpinner);
            }
        });

        sneezeSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyGrabber.grabNextKey(spinner2);
            }
        });

        final ActionListener sampleSizeHandler = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setMicrophoneFromUI();
                } catch (LineUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        };

        bit8.addActionListener(sampleSizeHandler);
        bit16.addActionListener(sampleSizeHandler);
    }

    private void setKeyFromUI() {
        presser.setKey((Integer) spinner1.getValue());
    }

    private void setMicrophoneFromUI() throws IllegalArgumentException, LineUnavailableException {

        if (microphoneWorker != null) {
            microphoneWorker.cancel(true);
        }

        int sampleSize = getSelectedSampleSize();
        initMicrophoneWorker((Mixer.Info) comboBox1.getSelectedItem(), sampleSize);

    }

    private int getSelectedSampleSize() {
        int sampleSize = 8;
        if (bit16.isSelected()) {
            sampleSize = 16;
        }
        return sampleSize;
    }

    private void setUIFromPreferences() {

        slider1.setValue(preferences.getInt(CONFIG_THRESHOLD, DEFAULT_THRESHOLD));
        spinner1.setValue(preferences.getInt(CONFIG_KEY_CODE, KeyEvent.VK_ALT));
        spinner2.setValue(preferences.getInt(CONFIG_SNEEZE_CODE, KeyEvent.VK_PAGE_DOWN));
        muteKeySpinner.setValue(preferences.getInt(CONFIG_MUTE_CODE, KeyEvent.VK_PAGE_UP));
        if (comboBox1.getItemCount() > 0) {
            comboBox1.setSelectedIndex(preferences.getInt(CONFIG_MIC_CHOICE, 0));
        }
        if (preferences.getInt(CONFIG_SAMPLE_CODE, DEFAULT_SAMPLE) == 16) {
            bit16.setSelected(true);
        }

    }

    private void savePreferences() {
        preferences.putInt(CONFIG_KEY_CODE, (Integer) spinner1.getValue());
        preferences.putInt(CONFIG_SNEEZE_CODE, (Integer) spinner2.getValue());
        preferences.putInt(CONFIG_MUTE_CODE, (Integer) muteKeySpinner.getValue());
        preferences.putInt(CONFIG_THRESHOLD, slider1.getValue());
        preferences.putInt(CONFIG_MIC_CHOICE, comboBox1.getSelectedIndex());
        preferences.putInt(CONFIG_SAMPLE_CODE, getSelectedSampleSize());
    }

    private void populateMicrophoneCombo() {

        List<Mixer.Info> mixers = AudioSystemHelper.ListAudioInputDevices();
        for (Mixer.Info mixer: mixers) {
            comboBox1.addItem(mixer);
        }

        if (comboBox1.getItemCount() == 0) {

            failForMissingMic();

        }

        comboBox1.setRenderer(new ListCellRenderer<Mixer.Info>() {
            @Override
            public Component getListCellRendererComponent(JList list, Mixer.Info value, int index, boolean isSelected, boolean cellHasFocus) {
                DefaultListCellRenderer renderer = new DefaultListCellRenderer();
                return renderer.getListCellRendererComponent(list, value.getName(), index, isSelected, cellHasFocus);
            }
        });
    }

    private void failForMissingMic() {
        JOptionPane.showMessageDialog(rootPanel, "No microphone detected. Please plug one in, wait ~20 seconds, and try again.");
        System.exit(-1);
    }


    private WindowListener getWindowListener() {
        return new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) { }

            @Override
            public void windowClosing(WindowEvent e) {

                microphoneWorker.cancel(true);
                presser.release();
                releaseSneezeHook();

                savePreferences();
            }

            @Override
            public void windowClosed(WindowEvent e) { }

            @Override
            public void windowIconified(WindowEvent e) { }

            @Override
            public void windowDeiconified(WindowEvent e) { }

            @Override
            public void windowActivated(WindowEvent e) {
                rootPanel.setVisible(true);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                rootPanel.setVisible(false);
            }
        };
    }



    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Essen
        }
        JFrame frame = new JFrame("Talk to Push");
        URL url = ClassLoader.getSystemResource("tarehart/alter/resources/alter.png");
        Image img = Toolkit.getDefaultToolkit().createImage(url);
        frame.setIconImage(img);

        try {
            AlterForm m = new AlterForm();
            //BorderForm b = new BorderForm();
            frame.setContentPane(m.rootPanel);
            //frame.setContentPane(b.panel1);
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
    private JPanel sneezeStatus;
    private JPanel configPanel;
    private JButton selectMuteKeyButton;
    private JSpinner muteKeySpinner;
    private JPanel muteStatus;
    private JRadioButton bit8;
    private JRadioButton bit16;
}
