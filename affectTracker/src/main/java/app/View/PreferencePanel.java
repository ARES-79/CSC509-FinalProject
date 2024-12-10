package app.View;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import app.Model.Blackboard;

public class PreferencePanel extends JPanel {
    
    private final JTextField emotionIpField;
    private final JTextField emotionPortField;
    private final JTextField eyeTrackingIpField;
    private final JTextField eyeTrackingPortField;
    private final JTextField maxHighlightsField;
    private final JTextField thresholdLengthField;
    
    public PreferencePanel() {
        setPreferredSize(new Dimension(1000, 250));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Blackboard blackboard = Blackboard.getInstance();
        
        // Emotion Server IP
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Emotion Server IP:"), gbc);
        emotionIpField = new JTextField(blackboard.getEmotionSocket_Host(), 20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(emotionIpField, gbc);
        
        // Emotion Server Port
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Emotion Server Port:"), gbc);
        emotionPortField = new JTextField(String.valueOf(blackboard.getEmotionSocket_Port()), 20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(emotionPortField, gbc);
        
        // Eye Tracking Server IP
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("ET Server IP:"), gbc);
        eyeTrackingIpField = new JTextField(blackboard.getEyeTrackingSocket_Host(), 20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(eyeTrackingIpField, gbc);
        
        // Eye Tracking Server Port
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("ET Port:"), gbc);
        eyeTrackingPortField = new JTextField(String.valueOf(blackboard.getEyeTrackingSocket_Port()), 20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(eyeTrackingPortField, gbc);
        
        // Max Highlights
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Max Highlights:"), gbc);
        maxHighlightsField = new JTextField(String.valueOf(blackboard.getMaxHighlights()), 20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(maxHighlightsField, gbc);
        
        // Threshold Length
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(new JLabel("Threshold Length:"), gbc);
        thresholdLengthField = new JTextField(String.valueOf(blackboard.getThresholdLength()), 20);  // Adjusted width for the field
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(thresholdLengthField, gbc);
        
        // Apply Button
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyChanges());
        add(applyButton, gbc);
    }
    
    private void applyChanges() {
        try {
            Blackboard blackboard = Blackboard.getInstance();
            blackboard.setEmotionSocket_Host(emotionIpField.getText());
            blackboard.setEmotionSocket_Port(Integer.parseInt(emotionPortField.getText()));
            blackboard.setEyeTrackingSocket_Host(eyeTrackingIpField.getText());
            blackboard.setEyeTrackingSocket_Port(Integer.parseInt(eyeTrackingPortField.getText()));
            int maxCircles = Integer.parseInt(maxHighlightsField.getText());
            int thresholdRadius = Integer.parseInt(thresholdLengthField.getText());
            blackboard.setMaxHighlights(maxCircles);
            blackboard.setThresholdLength(thresholdRadius);
            // logger.info ("Settings applied.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid ints for ports, Max Circles, and Threshold Radius.");
        }
    }
}
