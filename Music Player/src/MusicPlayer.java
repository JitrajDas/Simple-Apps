import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends JFrame {

    private Clip audioClip;
    private boolean isPlaying = false;
    private JButton playButton, stopButton, openButton, fastForwardButton, rewindButton, volumeUpButton, volumeDownButton, nextButton, prevButton;
    private JSlider volumeSlider;
    private JFileChooser fileChooser;
    private List<File> playlist = new ArrayList<>();
    private int currentTrackIndex = 0;

    public MusicPlayer() {
        // Set up the frame
        setTitle("Music Player");
        setPreferredSize(new Dimension(800, 300));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();

        // Create UI components
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        openButton = new JButton("Open Files");
        fastForwardButton = new JButton("Fast Forward");
        rewindButton = new JButton("Rewind");
        volumeUpButton = new JButton("+");
        volumeDownButton = new JButton("-");
        nextButton = new JButton("Next");
        prevButton = new JButton("Previous");
        volumeSlider = new JSlider(0, 100, 50);

        // Disable buttons until files are loaded
        playButton.setEnabled(false);
        stopButton.setEnabled(false);
        fastForwardButton.setEnabled(false);
        rewindButton.setEnabled(false);
        volumeUpButton.setEnabled(false);
        volumeDownButton.setEnabled(false);
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);

        // Set preferred size for the buttons
        playButton.setPreferredSize(new Dimension(100, 50));
        stopButton.setPreferredSize(new Dimension(100, 50));
        openButton.setPreferredSize(new Dimension(100, 50));
        fastForwardButton.setPreferredSize(new Dimension(120, 50));
        rewindButton.setPreferredSize(new Dimension(100, 50));
        volumeUpButton.setPreferredSize(new Dimension(50, 50));
        volumeDownButton.setPreferredSize(new Dimension(50, 50));
        nextButton.setPreferredSize(new Dimension(100, 50));
        prevButton.setPreferredSize(new Dimension(100, 50));
        volumeSlider.setPreferredSize(new Dimension(200, 50));

        // Create a file chooser
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);

        // Layout for the buttons
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setPreferredSize(new Dimension(800, 150));
        controlPanel.add(openButton);
        controlPanel.add(playButton);
        controlPanel.add(stopButton);
        controlPanel.add(rewindButton);
        controlPanel.add(fastForwardButton);
        controlPanel.add(prevButton);
        controlPanel.add(nextButton);

        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new FlowLayout());
        volumePanel.setPreferredSize(new Dimension(800, 100));
        volumePanel.add(volumeDownButton);
        volumePanel.add(volumeSlider);
        volumePanel.add(volumeUpButton);

        // Add panels to frame
        add(controlPanel, BorderLayout.CENTER);
        add(volumePanel, BorderLayout.SOUTH);

        // Action listener for the Open Files button
        openButton.addActionListener(e -> openFiles());

        // Action listener for the Play button
        playButton.addActionListener(e -> {
            if (!isPlaying) {
                playAudio();
            } else {
                pauseAudio();
            }
        });

        // Action listener for the Stop button
        stopButton.addActionListener(e -> stopAudio());

        // Action listener for the Fast Forward button
        fastForwardButton.addActionListener(e -> fastForward());

        // Action listener for the Rewind button
        rewindButton.addActionListener(e -> rewind());

        // Action listener for the Volume Up button
        volumeUpButton.addActionListener(e -> adjustVolume(5));

        // Action listener for the Volume Down button
        volumeDownButton.addActionListener(e -> adjustVolume(-5));

        // Action listener for the Next button
        nextButton.addActionListener(e -> nextTrack());

        // Action listener for the Previous button
        prevButton.addActionListener(e -> previousTrack());

        // Action listener for the Volume Slider
        volumeSlider.addChangeListener(e -> setVolume(volumeSlider.getValue()));

        // Pack the frame to adjust to preferred component sizes
        pack();
    }

    private void openFiles() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                playlist.add(file);
            }
            currentTrackIndex = 0;
            loadTrack(playlist.get(currentTrackIndex));
        }
    }

    private void loadTrack(File file) {
        try {
            if (audioClip != null && audioClip.isOpen()) {
                audioClip.close();
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            playButton.setEnabled(true);
            stopButton.setEnabled(true);
            fastForwardButton.setEnabled(true);
            rewindButton.setEnabled(true);
            volumeUpButton.setEnabled(true);
            volumeDownButton.setEnabled(true);
            nextButton.setEnabled(true);
            prevButton.setEnabled(true);
            playButton.setText("Play");
            isPlaying = false;
            setVolume(volumeSlider.getValue());

            // Listener to move to next track when the current track ends
            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && audioClip.getFramePosition() == audioClip.getFrameLength()) {
                    nextTrack();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }

    private void playAudio() {
        if (audioClip != null) {
            audioClip.start();
            isPlaying = true;
            playButton.setText("Pause");
        }
    }

    private void pauseAudio() {
        if (audioClip != null && isPlaying) {
            audioClip.stop();
            isPlaying = false;
            playButton.setText("Play");
        }
    }

    private void stopAudio() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.setFramePosition(0); // Reset the position to the start
            isPlaying = false;
            playButton.setText("Play");
        }
    }

    private void fastForward() {
        if (audioClip != null) {
            int currentFrame = audioClip.getFramePosition();
            int newPosition = Math.min(currentFrame + 500000, audioClip.getFrameLength()); // Adjust 500000 for speed
            audioClip.setFramePosition(newPosition);
            if (isPlaying) {
                audioClip.start();
            }
        }
    }

    private void rewind() {
        if (audioClip != null) {
            int currentFrame = audioClip.getFramePosition();
            int newPosition = Math.max(currentFrame - 500000, 0); // Adjust 500000 for speed
            audioClip.setFramePosition(newPosition);
            if (isPlaying) {
                audioClip.start();
            }
        }
    }

    private void nextTrack() {
        currentTrackIndex++;
        if (currentTrackIndex < playlist.size()) {
            loadTrack(playlist.get(currentTrackIndex));
            playAudio();
        } else {
            stopAudio();
            currentTrackIndex = 0; // Reset to the first track
        }
    }

    private void previousTrack() {
        currentTrackIndex--;
        if (currentTrackIndex >= 0) {
            loadTrack(playlist.get(currentTrackIndex));
            playAudio();
        } else {
            stopAudio();
            currentTrackIndex = playlist.size() - 1; // Go to the last track
        }
    }

    private void adjustVolume(int adjustment) {
        int currentVolume = volumeSlider.getValue();
        int newVolume = Math.max(0, Math.min(100, currentVolume + adjustment));
        volumeSlider.setValue(newVolume);
    }

    private void setVolume(int volume) {
        if (audioClip != null) {
            FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float range = max - min;
            float gain = min + (range * volume / 100);
            volumeControl.setValue(gain);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MusicPlayer player = new MusicPlayer();
            player.setVisible(true);
        });
    }
}
  
