package app.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.Data.Emotion;
import app.Data.ProcessedDataObject;

/**
 * The {@code RawDataProcessor} class processes both eye-tracking and emotion
 * data from queues.
 * It validates, processes, and then converts this data into
 * {@code ProcessedDataObject} instances
 * that can be used for further operations. The data includes integer
 * coordinates for eye-tracking
 * and float-based emotion scores, with an emphasis on identifying the prominent
 * emotion.
 * <p>
 * This class implements {@link Runnable} and is designed to run as a separate
 * thread.
 * <p>
 * The class relies on a {@link Blackboard} to retrieve data from the input
 * queues and add
 * processed data objects to the output queue.
 * 
 * <p>
 * Code Metrics:
 * - Number of Methods: 12
 * - Lines of Code (LOC): 160
 * - Cyclomatic Complexity: 7 (due to multiple conditional branches and error
 * handling)
 * - Number of Conditional Branches: 4 (in actionPerformed, propertyChange,
 * connectClients, and startServerThreads)
 * - Number of Loops: 0
 * </p>
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class RawDataProcessor implements Runnable, PropertyChangeListener {

   public static final String THREAD_NAME = "DataProcessor";
   private static final Logger LOGGER = LoggerFactory.getLogger(RawDataProcessor.class);
   private boolean running = false;

   public RawDataProcessor() {
      Blackboard.getInstance().addPropertyChangeListener(Blackboard.STOPPED, this);
      Blackboard.getInstance().addPropertyChangeListener(Blackboard.STARTED, this);
   }

   @Override
   public void run() {
      try {
         while (true) {
            while (running) {
               doYourWork();
            }
            Thread.sleep(1000);
         }
      } catch (InterruptedException e) {
         LOGGER.error(THREAD_NAME + " thread was interrupted", e);
         Thread.currentThread().interrupt();
      } catch (Exception e) {
         LOGGER.warn(e.toString());
      }
   }

   private void doYourWork() throws InterruptedException {
      // Poll with a timeout to prevent blocking indefinitely
      String eyeTrackingData = Blackboard.getInstance().pollEyeTrackingQueue();
      String emotionData = Blackboard.getInstance().pollEmotionQueue();
      if (eyeTrackingData != null) {
         LOGGER.info("ProcessingThread: Processing data pair: " + eyeTrackingData + ", " + emotionData);
         // Process the pair of data
         List<Integer> coordinates = convertToIntegerList(eyeTrackingData);
         List<Float> emotionScores = null;
         Emotion prominentEmotion;

         if (emotionData != null) {
            emotionScores = convertToFloatList(emotionData);
            // if the emotion data is invalid, use neutral
            if (!isValidEmotionData(emotionScores)) {
               logInvalidEmotionData(emotionData);
               prominentEmotion = Emotion.NONE;
            } else {
               prominentEmotion = getProminentEmotion(emotionScores);
            }
         } else {
            prominentEmotion = Emotion.NONE;
         }
         if (!isValidEyeTrackingData(coordinates)) {
            logInvalidEyeTrackingData(eyeTrackingData);
            return; // we can't do anything without eye tracking
         }
         ProcessedDataObject processedData = new ProcessedDataObject(
               coordinates.get(0),
               coordinates.get(1),
               prominentEmotion,
               emotionScores);
         LOGGER.info("Processed data created: " + processedData);
         Blackboard.getInstance().addToProcessedDataQueue(processedData);
      }
      // debugging client/server communication
      else if (emotionData != null) {
         // create a processed data object with no eye tracking data
         List<Float> emotionScores = convertToFloatList(emotionData);
         Emotion prominentEmotion = getProminentEmotion(emotionScores);
         ProcessedDataObject processedData = new ProcessedDataObject(
               -1,
               -1,
               prominentEmotion,
               emotionScores);
         LOGGER.info("Processed data created: " + processedData);
         Blackboard.getInstance().addToProcessedDataQueue(processedData);
      } else {
         // Handle timeout case or missing data
         LOGGER.warn(THREAD_NAME + ": Timed out waiting for data, or one client is slow.");
      }
   }

   private boolean isValidEyeTrackingData(List<Integer> data) {
      return data != null && data.stream().allMatch(number -> number >= 0);
   }

   private List<Integer> convertToIntegerList(String data) {
      try {
         // split on ":", remove the parens/brackets from {'gaze_point_on_display_area':
         // (335, 247)}
         data = data.split(":")[1].trim();
         data = data.replaceAll("[{}()']", "");
         // split on "," in "335, 247"
         return Arrays.stream(data.split(","))
               .map(String::trim)
               .map(Integer::parseInt)
               .collect(Collectors.toList());
      } catch (NumberFormatException e) {
         logInvalidEyeTrackingData(data);
         return null;
      }
   }

   private void logInvalidEyeTrackingData(String data) {
      LOGGER.warn("Eye-tracking data must be in the form \"int, int\"\n where both are >= 0." +
            "Invalid eye-tracking data format: " + data);
   }

   private boolean isValidEmotionData(List<Float> data) {
      return data != null && data.stream().allMatch(number -> (number >= 0 && number <= 1) || (number == -1));
   }

   private List<Float> convertToFloatList(String data) {
      try {
         return Arrays.stream(data.split(","))
               .map(String::trim)
               .map(Float::parseFloat)
               .collect(Collectors.toList());
      } catch (NumberFormatException e) {
         logInvalidEmotionData(data);
         return null; // Or return an empty list, or handle the error as needed
      }
   }

   public Emotion getProminentEmotion(List<Float> emotionScores) {
      if (emotionScores == null || emotionScores.isEmpty()) {
         throw new IllegalArgumentException("List must not be null or empty");
      }
      int maxIndex = 0; // Assume the first element is the largest initially
      for (int i = 1; i < emotionScores.size(); i++) {
         // If current element is greater than the current max, update maxIndex
         if (emotionScores.get(i) > emotionScores.get(maxIndex)) {
            maxIndex = i;
         }
      }
      if (emotionScores.get(maxIndex) == -1) {
         // No emotions were active
         return Emotion.NONE;
      }
      return Emotion.getByValue(maxIndex);
   }

   private void logInvalidEmotionData(String data) {
      LOGGER.warn("Emotion data is expected to be a comma seperated list of 5 floats between 0 and 1." +
            "Invalid emotion data format: " + data);
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case Blackboard.STOPPED -> {
            LOGGER.info("blackboard stopped, stopping rdp");
            running = false;
         }
         case Blackboard.STARTED -> {
            LOGGER.info("blackboard started, starting rdp");
            running = true;
         }
      }

   }
}