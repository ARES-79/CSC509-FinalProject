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

/**
 * The {@code PreferencePanel} class represents a configuration panel that
 * allows users to modify
 * server connection settings, display parameters, and system behavior. This
 * panel provides fields
 * for setting the MQTT broker, topics, max highlights, and threshold length. It
 * provides the user
 * with the ability to apply updated settings.
 * 
 * <p>
 * Code Metrics:
 * - Number of Methods: 5
 * - Lines of Code (LOC): 77
 * - Cyclomatic Complexity: 3 (due to try-catch and method calls in
 * applyChanges)
 * - Number of Conditional Branches: 1 (in the try-catch block for
 * NumberFormatException)
 * - Number of Loops: 0
 * 
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class PreferencePanel extends JPanel {

   private final JTextField mqttBrokerField;
   private final JTextField eyeTopicField;
   private final JTextField emotionTopicField;
   private final JTextField maxHighlightsField;
   private final JTextField thresholdLengthField;

   /**
    * Constructs the preference panel and initializes the UI components.
    * <p>
    * This includes text fields for setting the MQTT broker, eye and emotion
    * topics,
    * max highlights, and threshold length.
    */
   public PreferencePanel() {
      setPreferredSize(new Dimension(1000, 250));
      setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(10, 10, 10, 10);
      gbc.anchor = GridBagConstraints.WEST;

      Blackboard blackboard = Blackboard.getInstance();

      // MQTT Broker
      gbc.gridx = 0;
      gbc.gridy = 0;
      add(new JLabel("MQTT Broker:"), gbc);
      mqttBrokerField = new JTextField(blackboard.getMqttBroker(), 20);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      add(mqttBrokerField, gbc);

      // Eye Topic
      gbc.gridx = 0;
      gbc.gridy = 1;
      add(new JLabel("Eye Topic"), gbc);
      eyeTopicField = new JTextField(blackboard.getMqttEyeTopic(), 20);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      add(eyeTopicField, gbc);

      // Emotion Topic
      gbc.gridx = 0;
      gbc.gridy = 2;
      add(new JLabel("Emotion Topic"), gbc);
      emotionTopicField = new JTextField(blackboard.getMqttEmotionTopic(), 20);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      add(emotionTopicField, gbc);

      // Max Highlights
      gbc.gridx = 0;
      gbc.gridy = 3;
      add(new JLabel("Max Highlights:"), gbc);
      maxHighlightsField = new JTextField(String.valueOf(blackboard.getMaxHighlights()), 20);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      add(maxHighlightsField, gbc);

      // Threshold Length
      gbc.gridx = 0;
      gbc.gridy = 4;
      add(new JLabel("Threshold Length:"), gbc);
      thresholdLengthField = new JTextField(String.valueOf(blackboard.getThresholdLength()), 20);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      add(thresholdLengthField, gbc);

      // Apply Button
      gbc.gridx = 1;
      gbc.gridy = 5;
      gbc.gridwidth = 2;
      gbc.anchor = GridBagConstraints.CENTER;
      JButton applyButton = new JButton("Apply");
      applyButton.addActionListener(e -> applyChanges());
      add(applyButton, gbc);
   }

   /**
    * Applies the changes made in the preference fields to the {@link Blackboard}.
    * <p>
    * The method validates and updates the MQTT broker, eye and emotion topics, max
    * highlights,
    * and threshold length. If any invalid values are entered, it displays an error
    * message.
    */
   private void applyChanges() {
      try {
         Blackboard blackboard = Blackboard.getInstance();
         blackboard.setMqttBroker(mqttBrokerField.getText());
         blackboard.setMqttEyeTopic(eyeTopicField.getText());
         blackboard.setMqttEmotionTopic(emotionTopicField.getText());
         int maxCircles = Integer.parseInt(maxHighlightsField.getText());
         int thresholdRadius = Integer.parseInt(thresholdLengthField.getText());
         blackboard.setMaxHighlights(maxCircles);
         blackboard.setThresholdLength(thresholdRadius);
      } catch (NumberFormatException ex) {
         JOptionPane.showMessageDialog(this, "Please enter valid integers for Max Highlights and Threshold Length.");
      }
   }
}
