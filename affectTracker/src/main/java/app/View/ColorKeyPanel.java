package app.View;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import app.Model.Blackboard;

/**
 * The {@code ColorKeyPanel} class represents a panel that displays a key of emotion labels
 * and their corresponding colors. This panel provides a visual reference for users to understand
 * which colors correspond to specific emotions in the graphical interface.
 * Each label is paired with a colored square representing the emotion.
 */
public class ColorKeyPanel extends JPanel implements PropertyChangeListener {

    private JLabel[] frequencyLabels = new JLabel[5];  // Array to hold frequency labels for each emotion
    private static final String[] EMOTIONS = {
        "Focus", "Stress", "Engagement", "Excitement", "Interest"
    };
    private static final Color[] EMOTIONS_COLORS = {
        Color.YELLOW, Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };

    public ColorKeyPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Initialize frequency labels and add rows to the panel
        for (int i = 0; i < EMOTIONS.length; i++) {
            addRow(EMOTIONS[i], EMOTIONS_COLORS[i], "0%");
        }

        // Register this panel to listen to changes in the Blackboard
        Blackboard.getInstance().addPropertyChangeListener(Blackboard.PROPERTY_NAME_PROCESSED_DATA, this);
    }

    public void addRow(String emotion, Color emotionColor, String frequency) {
        // Create the emotion and frequency labels
        JLabel emotionLabel = new JLabel("<html><font color='" + getColorHex(emotionColor) + "'>" + emotion + "</font></html>");
        JLabel frequencyLabel = new JLabel("Frequency: " + frequency + "%");

        // Set the font for labels
        Font labelFont = new Font("Arial", Font.PLAIN, 14);
        emotionLabel.setFont(labelFont);
        frequencyLabel.setFont(labelFont);

        // Store the frequency labels to update later
        int index = getEmotionIndex(emotion);
        frequencyLabels[index] = frequencyLabel;

        // Create a panel to hold the emotion and frequency labels
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new GridLayout(1, 3));
        rowPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        rowPanel.add(emotionLabel);
        rowPanel.add(frequencyLabel);

        add(rowPanel);
    }

    private int getEmotionIndex(String emotion) {
        for (int i = 0; i < EMOTIONS.length; i++) {
            if (EMOTIONS[i].equals(emotion)) {
                return i;
            }
        }
        return -1;  // -1 if emotion is not found (error)
    }

    // Update frequencies from Blackboard and repaint the panel
    private void updateFrequencies() {
        try {
            List<String> frequencies = Blackboard.getInstance().getFrequencies();

            for (int i = 0; i < frequencies.size(); i++) {
                if (frequencyLabels[i] != null) {
                    frequencyLabels[i].setText("Frequency: " + frequencies.get(i));
                }
            }

            repaint();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Blackboard.PROPERTY_NAME_PROCESSED_DATA.equals(evt.getPropertyName())) {
            updateFrequencies(); 
        }
    }

    private String getColorHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
