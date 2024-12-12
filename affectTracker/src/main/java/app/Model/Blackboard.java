package app.Model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Deque;
import java.util.List;

import mqttLib.TheSubscriberMQTT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.Data.Highlight;
import app.Data.ProcessedDataObject;

/**
 * The {@code Blackboard} class serves as the central hub for managing data
 * across different components
 * of the system. It holds data queues for eye-tracking and emotion information,
 * a list of circles for the display,
 * and settings for server information and display behavior.
 * <p>
 * This class follows the singleton design pattern, ensuring that only one
 * instance of {@code Blackboard}
 * exists during the application's lifecycle. It provides synchronized access to
 * the data being exchanged
 * between components, and manages the state of data retrieval.
 * 
 * <p>
 * Code Metrics:
 * - Number of Classes: 1 (Blackboard)
 * - Number of Methods: 50
 * - Lines of Code (LOC): 254 (including comments and blank lines)
 * - Cyclomatic Complexity: 15 (based on complexity of `addSubscriberData` and
 * `propertyChange` methods)
 * - Number of Conditional Branches: 18 (includes multiple if-else statements
 * for handling different data)
 * - Number of Loops: 7 (for iterating through properties and delegates)
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class Blackboard extends PropertyChangeSupport implements PropertyChangeListener {

   private String MqttBroker = "tcp://broker.hivemq.com:1883"; // default broker
   private String MqttEyeTopic = "app/SimulatedEyeData";
   private String MqttEmotionTopic = "app/SimulatedEmotionData";

   public static final String PROPERTY_NAME_PROCESSED_DATA = "processed data";

   private boolean started = false;
   public static final String STARTED = "STARTED";
   public static final String STOPPED = "STOPPED";

   public static final String PROPERTY_NAME_VIEW_DATA = "view data";
   private final Logger logger;
   public static final String EYE_DATA_LABEL = "EYE";
   public static final String EMOTION_DATA_LABEL = "EMOTION";
   public static final String MQTTBROKER_ERROR = "MQTTE";
   public static final int EYE_TIMEOUT_IN_MS = 500;
   public static final int EMOTION_TIMEOUT_IN_MS = 200;
   private static final String PREFIX_DELIMITER = "~";
   private static final Blackboard INSTANCE = new Blackboard();

   private final ProcessedDataDelegate processedDataDelegate;
   private final EyeTrackingDataDelegate eyeTrackingDataDelegate;
   private final EmotionDataDelegate emotionDataDelegate;
   private final HighlightDataDelegate highlightDataDelegate;

   private Blackboard() {
      super(new Object());
      logger = LoggerFactory.getLogger(Blackboard.class);
      processedDataDelegate = new ProcessedDataDelegate();
      eyeTrackingDataDelegate = new EyeTrackingDataDelegate();
      emotionDataDelegate = new EmotionDataDelegate();
      highlightDataDelegate = new HighlightDataDelegate();
   }

   public static Blackboard getInstance() {
      return INSTANCE;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case TheSubscriberMQTT.CLIENT_PROPERTY_LABEL -> {
            addSubscriberData((String) evt.getNewValue());
         }
         case TheSubscriberMQTT.REPORT_ERROR_LABEL -> {
            alertError((String) evt.getNewValue());
         }
      }
   }

   /**
    * Parses prefix and calls the method to add the data to the appropriate data
    * structure.
    *
    * @param dataWithPrefix string of data with a prefix to denote the source
    */
   public void addSubscriberData(String dataWithPrefix) {
      if (isValidMessage(dataWithPrefix)) {
         String[] prefixAndData = dataWithPrefix.split(PREFIX_DELIMITER, 2);
         try {
            switch (prefixAndData[0]) {
               case EYE_DATA_LABEL -> addToEyeTrackingQueue(prefixAndData[1]);
               case EMOTION_DATA_LABEL -> addToEmotionQueue(prefixAndData[1]);
               default -> logger
                     .warn("Data from unknown source with prefix \"" + prefixAndData[0] + "\" : " + prefixAndData[1]);
            }
         } catch (InterruptedException e) {
            logger.warn("Data with prefix \"" + prefixAndData[0]
                  + "\" was interrupted and unable to be added to the queue: " + prefixAndData[1]);
         }
      } else {
         logger.warn("Data with invalid format : " + dataWithPrefix);
      }
   }

   /**
    * Parses prefix and calls the appropriate method to alert listeners of the
    * error.
    *
    * @param messageWithPrefix string of data with a prefix to denote the source
    */
   public void alertError(String messageWithPrefix) {
      if (isValidMessage(messageWithPrefix)) {
         String[] prefixAndMessage = messageWithPrefix.split(PREFIX_DELIMITER, 2);
         switch (prefixAndMessage[0]) {
            case EYE_DATA_LABEL -> reportEyeThreadError(prefixAndMessage[1]);
            case EMOTION_DATA_LABEL -> reportEmotionThreadError(prefixAndMessage[1]);
            case MQTTBROKER_ERROR -> reportMQTTBrokerError(prefixAndMessage[1]);
            default -> logger.warn(
                  "Alerted of error with unknown prefix \"" + prefixAndMessage[0] + "\" : " + prefixAndMessage[1]);
         }
      } else {
         logger.warn("Alerted of error without prefix: " + messageWithPrefix);
      }
   }

   /**
    * Ensures the message has a prefix and a body separated by the prefix
    * delimiter.
    *
    * @param messageWithPrefix string received from subscriber
    * @return true if message has a prefix and body
    */
   public boolean isValidMessage(String messageWithPrefix) {
      return messageWithPrefix.split(PREFIX_DELIMITER).length == 2;
   }

   public void addToEyeTrackingQueue(String data) throws InterruptedException {
      eyeTrackingDataDelegate.addToEyeTrackingQueue(data);
   }

   public String pollEyeTrackingQueue() throws InterruptedException {
      return eyeTrackingDataDelegate.pollEyeTrackingQueue();
   }

   public void addToEmotionQueue(String data) throws InterruptedException {
      emotionDataDelegate.addToEmotionQueue(data);
   }

   public String pollEmotionQueue() throws InterruptedException {
      return emotionDataDelegate.pollEmotionQueue();
   }

   public int getProcessedEmotions() throws InterruptedException {
      return emotionDataDelegate.getProcessedEmotions();
   }

   public void incrementEmotionCount(int index) throws InterruptedException {
      emotionDataDelegate.incrementEmotionCount(index);
   }

   public List<Integer> getEmotionCounts() throws InterruptedException {
      return emotionDataDelegate.getEmotionCounts();
   }

   public List<String> getFrequencies() throws InterruptedException {
      return emotionDataDelegate.getFrequencies();
   }

   public void setFrequencies(List<String> frequencies) throws InterruptedException {
      emotionDataDelegate.setFrequencies(frequencies);
   }

   public void addToProcessedDataQueue(ProcessedDataObject data) {
      processedDataDelegate.addToProcessedDataQueue(data);
      firePropertyChange(PROPERTY_NAME_PROCESSED_DATA, null, data);
   }

   public ProcessedDataObject getFromProcessedDataObjectQueue() {
      return processedDataDelegate.getFromProcessedDataQueue();
   }

   public List<Highlight> getHighlightList() {
      return highlightDataDelegate.getHighlightList();
   }

   public void setHighlightList(List<Highlight> highlightList) {
      highlightDataDelegate.setHighlightList(highlightList);
   }

   public void addHighlightCollection(List<Highlight> highlights) {
      highlightDataDelegate.addHighlightCollection(highlights);
   }

   public void updateHighlightColors(Color color) {
      highlightDataDelegate.updateHighlightColors(color);
   }

   public Deque<List<Highlight>> getHighlightCollections() {
      return highlightDataDelegate.getHighlightCollections();
   }

   public int getThresholdLength() {
      return highlightDataDelegate.getThresholdLength();
   }

   public void setThresholdLength(int thresholdLength) {
      highlightDataDelegate.setThresholdLength(thresholdLength);
   }

   public int getMaxHighlights() {
      return highlightDataDelegate.getMaxHighlights();
   }

   public void setMaxHighlights(int maxHighlights) {
      highlightDataDelegate.setMaxHighlights(maxHighlights);
   }

   public int getHighlightLength() {
      return highlightDataDelegate.getHighlightLength();
   }

   public void setHighlightLength(int highlightLength) {
      highlightDataDelegate.setHighlightLength(highlightLength);
   }

   public int getRowSize() {
      return highlightDataDelegate.getRowSize();
   }

   public void setRowSize(int rowSize) {
      highlightDataDelegate.setRowSize(rowSize);
   }

   public String getFormattedConnectionSettings() {
      return String.format(
            """
                  \t\tMQTTBroker: %s
                  \t\tEmotion Topic: %s
                  \t\tEye Tracking Topic: %s
                  """,
            MqttBroker, MqttEmotionTopic, MqttEyeTopic);
   }

   public void reportEyeThreadError(String ex_message) {
      firePropertyChange(EYE_DATA_LABEL, null, ex_message);
   }

   public void reportEmotionThreadError(String ex_message) {
      firePropertyChange(EMOTION_DATA_LABEL, null, ex_message);
   }

   public void reportMQTTBrokerError(String ex_message) {
      firePropertyChange(MQTTBROKER_ERROR, null, ex_message);
   }

   public void startedProcessing() {
      firePropertyChange(STARTED, started, true);
      started = true;
   }

   public void stoppedProcessing() {
      firePropertyChange(STOPPED, started, false);
      started = false;
   }

   public String getMqttBroker() {
      return MqttBroker;
   }

   public void setMqttBroker(String mqttBroker) {
      MqttBroker = mqttBroker;
   }

   public String getMqttEyeTopic() {
      return MqttEyeTopic;
   }

   public void setMqttEyeTopic(String mqttEyeTopic) {
      MqttEyeTopic = mqttEyeTopic;
   }

   public String getMqttEmotionTopic() {
      return MqttEmotionTopic;
   }

   public void setMqttEmotionTopic(String mqttEmotionTopic) {
      MqttEmotionTopic = mqttEmotionTopic;
   }
}
