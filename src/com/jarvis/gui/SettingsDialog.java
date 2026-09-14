package com.jarvis.gui;

import com.jarvis.audio.AudioPlayerService;
import com.jarvis.audio.AudioRecorderService;
import com.jarvis.core.ConfigManager;
import com.jarvis.speech.TextToSpeechService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Settings modal configuration dialog.
 * Manages persistent OpenAI API key, WhisperFlow continuous streaming mode,
 * TTS speech rate, and hardware test diagnostics.
 * Demonstrates MODAL DIALOGS, DATA BINDING, and USER PREFERENCE PERSISTENCE.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class SettingsDialog extends JDialog {

    private final JPasswordField apiKeyField;
    private final JCheckBox offlineModeCheck;
    private final JCheckBox continuousFlowCheck;
    private final JCheckBox soundEffectsCheck;
    private final JSlider ttsRateSlider;
    private final JTextField userNameField;
    private final JLabel micStatusLabel;

    public SettingsDialog(Frame parent, TextToSpeechService ttsService) {
        super(parent, "JARVIS Settings & AI Configuration", true);
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(13, 20, 38));
        setLayout(new BorderLayout(15, 15));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(13, 20, 38));
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        ConfigManager cfg = ConfigManager.getInstance();

        // 1. OpenAI Whisper API Section
        JLabel apiHeader = createSectionHeader("🔑 OpenAI Whisper Speech API");
        contentPanel.add(apiHeader);
        contentPanel.add(Box.createVerticalStrut(6));

        JLabel apiDesc = new JLabel("<html><span style='color: #8CA0BE; font-size: 9pt;'>Used by WhisperFlow to transcribe spoken microphone input.</span></html>");
        contentPanel.add(apiDesc);
        contentPanel.add(Box.createVerticalStrut(8));

        apiKeyField = new JPasswordField(cfg.getOpenAiApiKey());
        apiKeyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        apiKeyField.setBackground(new Color(22, 33, 56));
        apiKeyField.setForeground(Color.WHITE);
        apiKeyField.setCaretColor(Color.CYAN);
        apiKeyField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 180, 216), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        contentPanel.add(apiKeyField);
        contentPanel.add(Box.createVerticalStrut(12));

        // 2. WhisperFlow & Offline Modes
        JLabel modeHeader = createSectionHeader("⚙️ WhisperFlow Engine Modes");
        contentPanel.add(modeHeader);
        contentPanel.add(Box.createVerticalStrut(8));

        offlineModeCheck = new JCheckBox("Offline Lab Simulation Mode (For college viva without internet)", cfg.isOfflineMode());
        styleCheckBox(offlineModeCheck);
        contentPanel.add(offlineModeCheck);
        contentPanel.add(Box.createVerticalStrut(5));

        continuousFlowCheck = new JCheckBox("Enable Continuous WhisperFlow (Auto VAD listening)", cfg.isContinuousWhisperFlow());
        styleCheckBox(continuousFlowCheck);
        contentPanel.add(continuousFlowCheck);
        contentPanel.add(Box.createVerticalStrut(5));

        soundEffectsCheck = new JCheckBox("Synthesized Sound Effects & Alarms", cfg.isSoundEffectsEnabled());
        styleCheckBox(soundEffectsCheck);
        contentPanel.add(soundEffectsCheck);
        contentPanel.add(Box.createVerticalStrut(15));

        // 3. User & TTS Preferences
        JLabel ttsHeader = createSectionHeader("🗣️ Voice Synthesizer & Identity");
        contentPanel.add(ttsHeader);
        contentPanel.add(Box.createVerticalStrut(8));

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nameRow.setOpaque(false);
        JLabel nameLabel = new JLabel("User Name: ");
        nameLabel.setForeground(new Color(200, 220, 245));
        userNameField = new JTextField(cfg.getUserName(), 15);
        userNameField.setBackground(new Color(22, 33, 56));
        userNameField.setForeground(Color.WHITE);
        userNameField.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        nameRow.add(nameLabel);
        nameRow.add(userNameField);
        contentPanel.add(nameRow);
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel sliderLabel = new JLabel("Speech Synthesis Rate (-5 slow to +5 fast):");
        sliderLabel.setForeground(new Color(200, 220, 245));
        contentPanel.add(sliderLabel);

        ttsRateSlider = new JSlider(-5, 5, cfg.getTtsRate());
        ttsRateSlider.setOpaque(false);
        ttsRateSlider.setMajorTickSpacing(2);
        ttsRateSlider.setPaintTicks(true);
        ttsRateSlider.setForeground(Color.CYAN);
        contentPanel.add(ttsRateSlider);
        contentPanel.add(Box.createVerticalStrut(15));

        // 4. Hardware Diagnostics
        JPanel micRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        micRow.setOpaque(false);
        JButton testMicBtn = new JButton("🎙️ Test Hardware Mic");
        testMicBtn.setBackground(new Color(30, 50, 85));
        testMicBtn.setForeground(Color.WHITE);
        testMicBtn.setFocusPainted(false);

        boolean micAvailable = AudioRecorderService.isMicrophoneAvailable();
        micStatusLabel = new JLabel(micAvailable ? "✔ Microphone line detected" : "⚠ No microphone line found");
        micStatusLabel.setForeground(micAvailable ? new Color(0, 255, 170) : new Color(255, 100, 100));

        testMicBtn.addActionListener(e -> {
            boolean avail = AudioRecorderService.isMicrophoneAvailable();
            micStatusLabel.setText(avail ? "✔ Hardware Microphone Line Ready" : "❌ Microphone Line Unavailable");
            micStatusLabel.setForeground(avail ? new Color(0, 255, 170) : new Color(255, 100, 100));
            AudioPlayerService.playActivationBeep();
        });

        micRow.add(testMicBtn);
        micRow.add(micStatusLabel);
        contentPanel.add(micRow);

        add(contentPanel, BorderLayout.CENTER);

        // Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnBar.setBackground(new Color(9, 15, 28));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(25, 35, 55));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save Configuration");
        saveBtn.setBackground(new Color(0, 150, 220));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.addActionListener(e -> {
            cfg.setOpenAiApiKey(new String(apiKeyField.getPassword()));
            cfg.setOfflineMode(offlineModeCheck.isSelected());
            cfg.setContinuousWhisperFlow(continuousFlowCheck.isSelected());
            cfg.setSoundEffectsEnabled(soundEffectsCheck.isSelected());
            cfg.setTtsRate(ttsRateSlider.getValue());
            cfg.setUserName(userNameField.getText().trim());
            if (ttsService != null) {
                ttsService.setRate(ttsRateSlider.getValue());
            }
            dispose();
            JOptionPane.showMessageDialog(parent, "Settings successfully saved!", "JARVIS Config", JOptionPane.INFORMATION_MESSAGE);
        });

        btnBar.add(cancelBtn);
        btnBar.add(saveBtn);
        add(btnBar, BorderLayout.SOUTH);
    }

    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(0, 240, 255));
        return label;
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(new Color(220, 235, 255));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setFocusPainted(false);
    }
}
