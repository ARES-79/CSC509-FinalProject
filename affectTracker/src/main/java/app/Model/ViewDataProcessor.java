package app.Model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.Data.Circle;
import app.Data.Emotion;
import app.Data.Highlight;
import app.Data.ProcessedDataObject;

/**
 * The {@code ViewDataProcessor} is alerted of new processed data available in
 * the {@link Blackboard}, converts it into the appropriate {@link Circle} data
 * for visualization.
 * <p>
 * This class implements {@link PropertyChangeListener} and is intended to be
 * run as a separate thread.
 * It handles the consolidation of circles based on proximity and dynamically
 * updates the display.
 * <p>
 * The processor performs the following tasks:
 * <ul>
 * <li>Handles incoming processed data and converts it into highlights</li>
 * <li>Consolidates highlights if they are within a certain threshold</li>
 * <li>Updates the display by managing highlight colors and frequency data</li>
 * <li>Handles synchronization and property changes from the Blackboard</li>
 * </ul>
 *
 * Code Metrics:
 * - Number of Classes: 1 (ViewDataProcessor)
 * - Number of Methods: 6
 * (run, cleanUpThread, handleProcessedData, isWithinThreshold, updateFrequency,
 * propertyChange)
 * - Lines of Code (LOC): 106 (including comments and blank lines)
 * - Cyclomatic Complexity: 5
 * (based on method complexity: simple methods, plus handleProcessedData and
 * updateFrequency involving loops)
 * - Number of Conditional Branches: 7 (includes 'if' statements and loop for
 * consolidating highlights)
 * 
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class ViewDataProcessor implements Runnable, PropertyChangeListener {
   private static final String THREAD_NAME = "ViewLogic";
   private static final Logger LOGGER = LoggerFactory.getLogger(ViewDataProcessor.class);

   /**
    * Constructor for the {@code ViewDataProcessor}.
    * Registers this object as a listener for processed data changes on the
    * Blackboard.
    */
   public ViewDataProcessor() {
      super();
      Blackboard.getInstance().addPropertyChangeListener(Blackboard.PROPERTY_NAME_PROCESSED_DATA, this);
   }

   /**
    * Runs the ViewDataProcessor. Keeps the thread alive and idle while waiting for
    * new data.
    * This method is executed as a separate thread.
    */
   @Override
   public void run() {
      try {
         // Keep the thread alive and idle while waiting for new data
         synchronized (this) {
            wait();
         }
      } catch (InterruptedException e) {
         LOGGER.error(THREAD_NAME + " thread was interrupted", e);
         Thread.currentThread().interrupt();
      } catch (Exception e) {
         LOGGER.warn(e.toString());
      } finally {
         cleanUpThread();
      }
   }

   /**
    * Cleans up resources by removing this object as a listener from the
    * Blackboard.
    */
   public void cleanUpThread() {
      Blackboard.getInstance().removePropertyChangeListener(
            Blackboard.PROPERTY_NAME_PROCESSED_DATA, this);
   }

   /**
    * Handles incoming processed data, updates the highlights, and updates
    * frequency data.
    * 
    * @param data The {@link ProcessedDataObject} containing the processed data.
    * @throws InterruptedException If the thread is interrupted while handling the
    *                              data.
    */
   private void handleProcessedData(ProcessedDataObject data) throws InterruptedException {
      List<Highlight> highlightList = Blackboard.getInstance().getHighlightList();
      Color color = data.prominentEmotion().getColor();

      Highlight newHighlight = new Highlight(data.xCoord(), data.yCoord(), color,
            Blackboard.getInstance().getHighlightLength());
      highlightList.add(newHighlight);

      // Consolidate highlights if they are within a specified threshold
      for (Highlight highlight : highlightList) {
         if (isWithinThreshold(highlight, newHighlight)) {
            highlight.increaseLength(50); // Consolidate by increasing the length
            break;
         }
      }

      if (data.prominentEmotion() != Emotion.NONE) {
         LOGGER.info("Updating highlight colors");
         Blackboard.getInstance().updateHighlightColors(color);
         Blackboard.getInstance().addHighlightCollection(highlightList);
         updateFrequency(data.prominentEmotion());
      }

      Blackboard.getInstance().setHighlightList(highlightList);
   }

   /**
    * Determines whether two highlights are within a threshold distance of each
    * other.
    * 
    * @param existing     The existing highlight.
    * @param newHighlight The new highlight to compare.
    * @return True if the highlights are within the threshold distance; otherwise,
    *         false.
    */
   private boolean isWithinThreshold(Highlight existing, Highlight newHighlight) {
      return Math.abs(existing.getX() - newHighlight.getX()) <= Blackboard.getInstance().getThresholdLength() &&
            Math.abs(existing.getY() - newHighlight.getY()) <= Blackboard.getInstance().getThresholdLength();
   }

   /**
    * Updates the frequency of emotions based on the processed data.
    * This method updates the count of each emotion and recalculates the
    * percentage.
    * 
    * @param emotion The emotion whose frequency is being updated.
    * @throws InterruptedException If the thread is interrupted while updating the
    *                              frequency.
    */
   private void updateFrequency(Emotion emotion) throws InterruptedException {
      int index = emotion.getValue();
      Blackboard.getInstance().incrementEmotionCount(index);

      List<String> frequencies = new ArrayList<>();
      List<Integer> emotionCounts = Blackboard.getInstance().getEmotionCounts();
      int totalProcessedEmotions = Blackboard.getInstance().getProcessedEmotions();
      for (int count : emotionCounts) {
         int percentage = (int) ((double) count / totalProcessedEmotions * 100);
         frequencies.add(percentage + "%");
      }
      Blackboard.getInstance().setFrequencies(frequencies);
   }

   /**
    * Handles property change events triggered by updates in the Blackboard.
    * This method is called whenever processed data is available.
    * 
    * @param evt The property change event containing the new processed data.
    */
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (Blackboard.PROPERTY_NAME_PROCESSED_DATA.equals(evt.getPropertyName())) {
         ProcessedDataObject data = (ProcessedDataObject) evt.getNewValue();
         if (data == null) {
            LOGGER.warn("Received null ProcessedDataObject");
            return;
         }
         LOGGER.info("Received ProcessedDataObject: " + data);
         try {
            handleProcessedData(data);
         } catch (InterruptedException e) {
            LOGGER.error("Error handling processed data", e);
            Thread.currentThread().interrupt();
         }
      }
   }
}
