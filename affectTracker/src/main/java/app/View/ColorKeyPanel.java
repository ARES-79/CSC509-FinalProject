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
import javax.swing.border.MatteBorder;

import app.Model.Blackboard;

/**
 * The {@code ColorKeyPanel} class represents a panel that displays a key of emotion labels
 * and their corresponding colors. This panel provides a visual reference for users to understand
 * which colors correspond to specific emotions in the graphical interface.
 * Each label is paired with a colored square representing the emotion.
 */
public class ColorKeyPanel extends JPanel implements PropertyChangeListener {

    private JLabel[] frequencyLabels = new JLabel[6];  // Array to hold frequency labels for each emotion
    /*
     * 	ATTENTION (Color.YELLOW, 0),
	ENGAGEMENT (Color.GREEN, 1),
	EXCITEMENT (Color.BLUE, 2),
	STRESS (Color.RED, 3),
	RELAXATION (Color.MAGENTA, 4),
	INTEREST (Color.CYAN, 5);
     */
    private static final String[] EMOTIONS = {
        "Attention", "Engagement", "Excitement", "Stress", "Relaxation", "Interest"
    };
    private static final Color[] EMOTIONS_COLORS = {
        Color.YELLOW, Color.GREEN, Color.BLUE, Color.RED, Color.MAGENTA, Color.CYAN
    };

    public ColorKeyPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new MatteBorder(3, 1, 3, 3, Color.BLACK));

        //addHeaderRow();

        // Initialize frequency labels and add rows to the panel
        for (int i = 0; i < EMOTIONS.length; i++) {
            addRow(EMOTIONS[i], EMOTIONS_COLORS[i], "0%");
        }

        // Register this panel to listen to changes in the Blackboard
        Blackboard.getInstance().addPropertyChangeListener(Blackboard.PROPERTY_NAME_PROCESSED_DATA, this);
    }

    private void addHeaderRow() {
        JLabel headerLabel = new JLabel("Frequencies");
        Font headerFont = new Font("Arial", Font.BOLD, 16);
        headerLabel.setFont(headerFont);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(1, 1));
        headerPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        headerPanel.add(headerLabel);

        add(headerPanel);
    }

    public void addRow(String emotion, Color emotionColor, String frequency) {
        // Create the emotion and frequency labels
        String emotionText = String.format("<html><span style='border: 2px solid black; color: %s;'>%s</span></html>" 
                                          ,getColorHex(emotionColor), emotion);
        JLabel emotionLabel = new JLabel(emotionText);
        JLabel frequencyLabel = new JLabel(frequency);

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
        //rowPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

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
                    frequencyLabels[i].setText(frequencies.get(i));
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
