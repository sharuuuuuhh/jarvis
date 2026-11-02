package com.jarvis.gui;

import com.jarvis.audio.AudioPlayerService;
import com.jarvis.audio.AudioRecorderService;
import com.jarvis.core.ConfigManager;
import com.jarvis.speech.TextToSpeechService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Preferences dialog styled to match the JARVIS dashboard. */
public class SettingsDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Color PAGE = new Color(247, 246, 242);
    private static final Color SURFACE = new Color(250, 249, 246);
    private static final Color BORDER = new Color(224, 222, 214);
    private static final Color INK = new Color(24, 30, 31);
    private static final Color MUTED = new Color(119, 128, 130);
    private static final Color ACCENT = new Color(190, 111, 87);
    private final JPasswordField apiKeyField;
    private final JCheckBox offlineModeCheck;
    private final JCheckBox continuousFlowCheck;
    private final JCheckBox soundEffectsCheck;
    private final JCheckBox voiceOutputCheck;
    private final JSlider ttsRateSlider;
    private final JTextField userNameField;
    private final JLabel micStatusLabel;

    public SettingsDialog(Frame parent, TextToSpeechService ttsService) {
        super(parent, "JARVIS Settings", true);
        setSize(560, 680);
        setMinimumSize(new Dimension(520, 600));
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(PAGE);
        setLayout(new BorderLayout());

        ConfigManager cfg = ConfigManager.getInstance();
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(PAGE);
        content.setBorder(new EmptyBorder(26, 30, 24, 30));

        JLabel eyebrow = new JLabel("PREFERENCES");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(MUTED);
        JLabel heading = new JLabel("Settings");
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        heading.setForeground(INK);
        heading.setIcon(JarvisIcons.of("settings", ACCENT, 21));
        heading.setIconTextGap(10);
        content.add(eyebrow);
        content.add(Box.createVerticalStrut(6));
        content.add(heading);
        content.add(Box.createVerticalStrut(18));

        JPanel apiCard = card();
        apiCard.setLayout(new BoxLayout(apiCard, BoxLayout.Y_AXIS));
        apiCard.add(sectionTitle("Speech recognition"));
        apiCard.add(Box.createVerticalStrut(5));
        apiCard.add(description("Add an API key to enable online Whisper transcription."));
        apiCard.add(Box.createVerticalStrut(10));
        JPanel keyRow = new JPanel(new BorderLayout(8, 0));
        keyRow.setOpaque(false);
        apiKeyField = new JPasswordField(cfg.getOpenAiApiKey());
        styleField(apiKeyField);
        JButton showKey = textButton("Show");
        showKey.addActionListener(e -> {
            if (apiKeyField.getEchoChar() == 0) {
                apiKeyField.setEchoChar('•');
                showKey.setText("Show");
            } else {
                apiKeyField.setEchoChar((char) 0);
                showKey.setText("Hide");
            }
        });
        keyRow.add(apiKeyField, BorderLayout.CENTER);
        keyRow.add(showKey, BorderLayout.EAST);
        apiCard.add(keyRow);
        content.add(apiCard);
        content.add(Box.createVerticalStrut(12));

        JPanel modeCard = card();
        modeCard.setLayout(new BoxLayout(modeCard, BoxLayout.Y_AXIS));
        modeCard.add(sectionTitle("Assistant behavior"));
        modeCard.add(Box.createVerticalStrut(8));
        offlineModeCheck = check("Offline mode (voice recognition unavailable)", cfg.isOfflineMode());
        continuousFlowCheck = check("Continuous listening with voice activity detection", cfg.isContinuousWhisperFlow());
        soundEffectsCheck = check("Sound effects and reminder alarms", cfg.isSoundEffectsEnabled());
        modeCard.add(offlineModeCheck);
        modeCard.add(Box.createVerticalStrut(5));
        modeCard.add(continuousFlowCheck);
        modeCard.add(Box.createVerticalStrut(5));
        modeCard.add(soundEffectsCheck);
        content.add(modeCard);
        content.add(Box.createVerticalStrut(12));

        JPanel voiceCard = card();
        voiceCard.setLayout(new BoxLayout(voiceCard, BoxLayout.Y_AXIS));
        voiceCard.add(sectionTitle("Voice and profile"));
        voiceCard.add(Box.createVerticalStrut(10));
        JPanel voiceOutputRow = new JPanel(new BorderLayout(8, 0));
        voiceOutputRow.setOpaque(false);
        voiceOutputCheck = check("Speak JARVIS responses aloud", cfg.isTtsEnabled());
        JButton testVoice = textButton("Test voice");
        testVoice.addActionListener(e -> {
            if (ttsService != null) {
                ttsService.speak("Voice output is working. JARVIS will speak its responses aloud.");
            } else {
                JOptionPane.showMessageDialog(parent, "Voice output is unavailable.", "JARVIS Voice", JOptionPane.WARNING_MESSAGE);
            }
        });
        voiceOutputRow.add(voiceOutputCheck, BorderLayout.CENTER);
        voiceOutputRow.add(testVoice, BorderLayout.EAST);
        voiceCard.add(voiceOutputRow);
        voiceCard.add(Box.createVerticalStrut(10));
        JPanel nameRow = new JPanel(new BorderLayout(10, 0));
        nameRow.setOpaque(false);
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setForeground(INK);
        userNameField = new JTextField(cfg.getUserName());
        styleField(userNameField);
        nameRow.add(nameLabel, BorderLayout.WEST);
        nameRow.add(userNameField, BorderLayout.CENTER);
        voiceCard.add(nameRow);
        voiceCard.add(Box.createVerticalStrut(12));
        JPanel rateHeader = new JPanel(new BorderLayout());
        rateHeader.setOpaque(false);
        JLabel rateLabel = new JLabel("Speech rate");
        rateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rateLabel.setForeground(INK);
        JLabel rateHint = new JLabel("−5 slower · +5 faster");
        rateHint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        rateHint.setForeground(MUTED);
        rateHeader.add(rateLabel, BorderLayout.WEST);
        rateHeader.add(rateHint, BorderLayout.EAST);
        voiceCard.add(rateHeader);
        ttsRateSlider = new JSlider(-5, 5, cfg.getTtsRate());
        ttsRateSlider.setOpaque(false);
        ttsRateSlider.setMajorTickSpacing(2);
        ttsRateSlider.setPaintTicks(true);
        ttsRateSlider.setForeground(ACCENT);
        voiceCard.add(ttsRateSlider);
        content.add(voiceCard);
        content.add(Box.createVerticalStrut(12));

        JPanel diagnosticsCard = card();
        diagnosticsCard.setLayout(new BorderLayout(12, 0));
        JPanel diagnosticInfo = new JPanel();
        diagnosticInfo.setOpaque(false);
        diagnosticInfo.setLayout(new BoxLayout(diagnosticInfo, BoxLayout.Y_AXIS));
        diagnosticInfo.add(sectionTitle("Microphone"));
        boolean available = AudioRecorderService.isMicrophoneAvailable();
        micStatusLabel = new JLabel(available ? "Microphone detected" : "No microphone detected");
        micStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        micStatusLabel.setForeground(available ? new Color(83, 137, 99) : new Color(166, 74, 63));
        diagnosticInfo.add(Box.createVerticalStrut(4));
        diagnosticInfo.add(micStatusLabel);
        JButton testMic = textButton("Test mic");
        testMic.addActionListener(e -> {
            boolean detected = AudioRecorderService.isMicrophoneAvailable();
            micStatusLabel.setText(detected ? "Microphone is ready" : "Microphone is unavailable");
            micStatusLabel.setForeground(detected ? new Color(83, 137, 99) : new Color(166, 74, 63));
            if (detected) AudioPlayerService.playActivationBeep();
        });
        diagnosticsCard.add(diagnosticInfo, BorderLayout.CENTER);
        diagnosticsCard.add(testMic, BorderLayout.EAST);
        content.add(diagnosticsCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 12));
        footer.setBackground(PAGE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        JButton cancel = textButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = new JButton("Save settings");
        save.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        save.setForeground(Color.WHITE);
        save.setBackground(ACCENT);
        save.setFocusPainted(false);
        save.setBorder(new EmptyBorder(9, 16, 9, 16));
        save.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        save.addActionListener(e -> {
            cfg.setOpenAiApiKey(new String(apiKeyField.getPassword()));
            cfg.setOfflineMode(offlineModeCheck.isSelected());
            cfg.setContinuousWhisperFlow(continuousFlowCheck.isSelected());
            cfg.setSoundEffectsEnabled(soundEffectsCheck.isSelected());
            cfg.setTtsRate(ttsRateSlider.getValue());
            cfg.setTtsEnabled(voiceOutputCheck.isSelected());
            cfg.setUserName(userNameField.getText().trim());
            if (ttsService != null) ttsService.setRate(ttsRateSlider.getValue());
            dispose();
            JOptionPane.showMessageDialog(parent, "Your settings have been saved.", "JARVIS Settings", JOptionPane.INFORMATION_MESSAGE);
        });
        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(14, 15, 14, 15)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(INK);
        return label;
    }

    private JLabel description(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(MUTED);
        return label;
    }

    private JCheckBox check(String text, boolean selected) {
        JCheckBox check = new JCheckBox(text, selected);
        check.setOpaque(false);
        check.setForeground(INK);
        check.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        check.setFocusPainted(false);
        return check;
    }

    private void styleField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        field.setForeground(INK);
        field.setBackground(SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 9, 7, 9)));
        field.setPreferredSize(new Dimension(100, 34));
    }

    private JButton textButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(new Color(75, 87, 86));
        button.setBackground(SURFACE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 11, 7, 11)));
        return button;
    }
}
