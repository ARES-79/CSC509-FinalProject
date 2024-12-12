package app.View;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import app.Model.Blackboard;

/**
 * The {@code ColorKeyPanel} class represents a panel that displays a key of
 * emotion labels and their corresponding colors. This panel provides a visual
 * reference for
 * users to understand which colors correspond to specific emotions in the
 * graphical interface.
 * Each label is paired with a colored square representing the emotion.
 * <p>
 * This panel listens to property change events and updates the frequency values
 * displayed next to each emotion label.
 *
 * Code Metrics:
 * - Number of Classes: 1 (ColorKeyPanel)
 * - Number of Methods: 7 (constructor, addRow, getEmotionIndex,
 * updateFrequencies, propertyChange, getColorHex)
 * - Lines of Code (LOC): 80 (including comments and blank lines)
 * - Cyclomatic Complexity: 4 (based on method complexity: simple methods, plus
 * the conditional logic in `propertyChange`)
 * - Number of Conditional Branches: 3 (includes `if` statements and a loop for
 * updating frequencies)
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class ColorKeyPanel extends JPanel implements PropertyChangeListener {

   private JLabel[] frequencyLabels = new JLabel[6]; // Array to hold frequency labels for each emotion
   private static final String[] EMOTIONS = {
         "Attention", "Engagement", "Excitement", "Stress", "Relaxation", "Interest"
   };
   private static final Color[] EMOTIONS_COLORS = {
         Color.PINK, Color.GREEN, Color.BLUE, Color.RED, Color.MAGENTA, Color.CYAN
   };

   /**
    * Constructor for the {@code ColorKeyPanel}. Initializes the panel layout and
    * labels for each emotion.
    * It also registers this panel as a listener for processed data updates from
    * the Blackboard.
    */
   public ColorKeyPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(new MatteBorder(3, 1, 3, 3, Color.BLACK));

      // Initialize frequency labels and add rows to the panel
      for (int i = 0; i < EMOTIONS.length; i++) {
         addRow(EMOTIONS[i], EMOTIONS_COLORS[i], "0%");
      }

      // Register this panel to listen to changes in the Blackboard
      Blackboard.getInstance().addPropertyChangeListener(Blackboard.PROPERTY_NAME_VIEW_DATA, this);
   }

   /**
    * Adds a row for each emotion displaying its color and frequency percentage.
    * 
    * @param emotion      The name of the emotion.
    * @param emotionColor The color associated with the emotion.
    * @param frequency    The frequency of the emotion as a percentage.
    */
   public void addRow(String emotion, Color emotionColor, String frequency) {
      // Create the emotion and frequency labels
      String emotionText = String.format("<html><span style='border: 2px solid black; color: %s;'>%s</span></html>",
            getColorHex(emotionColor), emotion);
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

      rowPanel.add(emotionLabel);
      rowPanel.add(frequencyLabel);

      add(rowPanel);
   }

   /**
    * Retrieves the index of a given emotion in the EMOTIONS array.
    * 
    * @param emotion The name of the emotion.
    * @return The index of the emotion, or -1 if the emotion is not found.
    */
   private int getEmotionIndex(String emotion) {
      for (int i = 0; i < EMOTIONS.length; i++) {
         if (EMOTIONS[i].equals(emotion)) {
            return i;
         }
      }
      return -1; // -1 if emotion is not found (error)
   }

   /**
    * Updates the frequency labels with the current values from the Blackboard.
    * This method is called when new processed data is available.
    */
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

   /**
    * Handles property change events triggered by new processed data. It updates
    * the frequency labels when new processed data is received.
    * 
    * @param evt The property change event containing the new data.
    */
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (Blackboard.PROPERTY_NAME_VIEW_DATA.equals(evt.getPropertyName())) {
         updateFrequencies();
      }
   }

   /**
    * Converts a {@link Color} object to a hexadecimal string format.
    * 
    * @param color The color to convert.
    * @return A string representing the color in hexadecimal format.
    */
   private String getColorHex(Color color) {
      return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
   }
}
