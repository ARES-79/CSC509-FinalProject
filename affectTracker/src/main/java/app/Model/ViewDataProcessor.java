package app.Model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.Data.Circle;
import app.Data.Emotion;
import app.Data.Highlight;
import app.Data.ProcessedDataObject;

/**
 * The {@code ViewDataProcessor} is alerted of new processed data available in the {@link Blackboard},
 * converts it into the appropriate {@link Circle} data for visualization.
 * <p>
 * This class implements {@link PropertyChangeListener} and is intended to be run as a separate thread.
 * It handles the consolidation of circles based on proximity and dynamically updates the display.
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class ViewDataProcessor implements Runnable, PropertyChangeListener {
	private static final String THREAD_NAME = "ViewLogic";
	private static final Logger LOGGER = LoggerFactory.getLogger(RawDataProcessor.class);

	public ViewDataProcessor() {
		super();
		Blackboard.getInstance().addPropertyChangeListener(Blackboard.PROPERTY_NAME_PROCESSED_DATA, this);
	}

	@Override
	public void run() {
		try {
			//keep the thread alive and idle while waiting for new data
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
	
//	@Override
	public void cleanUpThread() {
		Blackboard.getInstance().removePropertyChangeListener(
			Blackboard.PROPERTY_NAME_PROCESSED_DATA, this);
	}
	
	private void handleProcessedData(ProcessedDataObject data) throws InterruptedException {
      Deque<Highlight> highlightList = Blackboard.getInstance().getHighlightList();
		Color color = data.prominentEmotion().getColor();
      // form frequency values based on prominent colors

      Highlight newHighlight = new Highlight(data.xCoord(), data.yCoord(), color, Blackboard.getInstance().getHighlightLength());
		boolean consolidated = false;

      for (Highlight highlight : highlightList) {
         if (isWithinThreshold(highlight, newHighlight)) {
            highlight.increaseLength(50); // Consolidate by increasing the length
            consolidated = true;
            break;
         }
      }
      if (!consolidated) {
         if (highlightList.size() == Blackboard.getInstance().getMaxHighlights()) {
            highlightList.pollFirst();
         }
         highlightList.addLast(newHighlight); // Add the new highlight
         // Update frequency with highlighed emotion
         Emotion highlightEmotion = Emotion.getEmotionByColor(newHighlight.getColor());
         if (highlightEmotion != Emotion.NONE) {
            updateFrequency(highlightEmotion);
         }
      }
      Blackboard.getInstance().setHighlightList(highlightList);
	}

   private boolean isWithinThreshold(Highlight existing, Highlight newHighlight) {
      return Math.abs(existing.getX() - newHighlight.getX()) <= Blackboard.getInstance().getThresholdLength() &&
             Math.abs(existing.getY() - newHighlight.getY()) <= Blackboard.getInstance().getThresholdLength();
   }

    private void updateFrequency(Emotion emotion) throws InterruptedException {
      List<String> frequencies = Blackboard.getInstance().getFrequencies();
      int index = emotion.ordinal(); // Get the index of the emotion (0-4)
      int currentCount = Integer.parseInt(frequencies.get(index).replace("%", ""));
      currentCount++;

      // Update the frequency for this emotion
      frequencies.set(index, currentCount + "%");

      Blackboard.getInstance().incrementEmotions();

      // Update frequency as percentages
      for (int i = 0; i < frequencies.size(); i++) {
         int count = Integer.parseInt(frequencies.get(i).replace("%", ""));
         int percentage = (int) ((double) count / Blackboard.getInstance().getProcessedEmotions() * 100);
         frequencies.set(i, percentage + "%");
     }

     Blackboard.getInstance().setFrequencies(frequencies);
   }
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		ProcessedDataObject data = (ProcessedDataObject) evt.getNewValue();
		System.out.println("retrieved processed data: " + data);
		if (data != null) {
         try {
            handleProcessedData(data);
         } catch (Exception e) {
            LOGGER.error("Error processing data", e);
         }
		}
	}

}