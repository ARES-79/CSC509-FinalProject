package app.Model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.Data.Circle;
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
	
	private void handleProcessedData(ProcessedDataObject data) {
      Deque<Highlight> highlightList = Blackboard.getInstance().getHighlightList();
		//Deque<Circle> circleList = Blackboard.getInstance().getCircleList();
		Color color = data.prominentEmotion().getColor();
      // form frequency values based on prominent colors

      Highlight newHighlight = new Highlight(data.xCoord(), data.yCoord(), color, Blackboard.getInstance().getHighlightLength());
		//Circle newCircle = new Circle(data.xCoord(), data.yCoord(), color, Blackboard.getInstance().getCircleRadius());
		boolean consolidated = false;
      /* 
		for (Circle circle : circleList) {
			if (isWithinThreshold(circle, newCircle)) {
				circle.increaseRadius(50); // Consolidate by increasing the radius
				consolidated = true;
				break;
			}
		}
      /* 
      for (Highlight highlight : highlightList) {
         if (isWithinThreshold(highlight, newHighlight)) {
            highlight.increaseLength(50); // Consolidate by increasing the length
            consolidated = true;
            break;
         }
      }
      */
      /* 
		if (!consolidated) {
			if (circleList.size() == Blackboard.getInstance().getMaxCircles()) {
				circleList.pollFirst();
			}
			circleList.addLast(newCircle); // Add the new circle
		}
      */
      //if (!consolidated) {
      if (highlightList.size() == Blackboard.getInstance().getMaxHighlights()) {
         highlightList.pollFirst();
      }
      highlightList.addLast(newHighlight); // Add the new highlight
      //}
      Blackboard.getInstance().setHighlightList(highlightList);
		//Blackboard.getInstance().setCircleList(circleList);
	}
	
	private boolean isWithinThreshold(Circle existing, Circle newCircle) {
		int dx = existing.getX() - newCircle.getX();
		int dy = existing.getY() - newCircle.getY();
		double distance = Math.sqrt(dx * dx + dy * dy);
		return distance <= Blackboard.getInstance().getThresholdRadius();
	}

   private boolean isWithinThreshold(Highlight existing, Highlight newHighlight) {
      return Math.abs(existing.getX() - newHighlight.getX()) <= Blackboard.getInstance().getThresholdLength() &&
             Math.abs(existing.getY() - newHighlight.getY()) <= Blackboard.getInstance().getThresholdLength();
   }
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		ProcessedDataObject data = (ProcessedDataObject) evt.getNewValue();
		System.out.println("retrieved processed data: " + data);
		if (data != null) {
			handleProcessedData(data);
		}
	}

}