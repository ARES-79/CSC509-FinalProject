package mqttLib;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to connect to a caller defined MQTT broker, with a caller defined
 * ClientID.
 * A {@code Map<String, String} holds pairs of topics to subscribe to and the
 * corresponding prefix
 * that should be used when sending the data to its destination for parsing.
 * The connection is attempted within the constructor so that the connection is
 * sure to exist before it is
 * * run as a thread.
 * <p>
 * When running within a Thread, TheSubscriberMQTT simply waits for 'mail' from
 * the broker
 * and processes it by adding the prefix to the message and sending it to its
 * destination.
 * 
 * Code Metrics:
 * - Number of Methods: 6
 * - Lines of Code (LOC): 94
 * - Cyclomatic Complexity: 5 (due to try-catch and loop)
 * - Number of Conditional Branches: 5 (in `run`, `connectionLost`,
 * `messageArrived`, and `stopSubscriber` methods)
 * - Number of Loops: 1 (in `run` method)
 * </p>
 */
public class TheSubscriberMQTT extends PropertyChangeSupport implements Runnable, MqttCallback {

   private final Logger log = LoggerFactory.getLogger(TheSubscriberMQTT.class.getName());
   private final Map<String, String> topicAndPrefixPairs;

   public static final String CLIENT_PROPERTY_LABEL = "addClientData";
   public static final String REPORT_ERROR_LABEL = "reportSubscriberError";
   public static final String MQTT_CONNECTED_LABEL = "mqttConnected";

   private static final String MQTT_PREFIX = "MQTTE";
   private static final String PREFIX_DELIMITER = "~";

   private final String broker;
   private final String clientID;
   private boolean running = true;

   public TheSubscriberMQTT(String broker, String clientID, Map<String, String> topicAndPrefixPairs,
         PropertyChangeListener listener) {
      super(new Object());
      this.broker = broker;
      this.clientID = clientID;
      this.topicAndPrefixPairs = topicAndPrefixPairs;
      this.addPropertyChangeListener(CLIENT_PROPERTY_LABEL, listener);
      this.addPropertyChangeListener(REPORT_ERROR_LABEL, listener);
      this.addPropertyChangeListener(MQTT_CONNECTED_LABEL, listener);
   }

   @Override
   public void run() {
      try (MqttClient client = new MqttClient(broker, clientID);) {
         log.debug("in constructing try block of mqtt subscriber");
         client.setCallback(this);
         log.debug("right before connecting to broker");
         client.connect();
         log.info("Connected to broker: " + broker);
         for (String topic : topicAndPrefixPairs.keySet()) {
            client.subscribe(topic);
            firePropertyChange(MQTT_CONNECTED_LABEL, null, "MQTT Connected: " + topic);
            log.info("Subscribed to topic: " + topic);
         }
         // keep the thread alive and idle while waiting for new data
         while (running) {
            Thread.sleep(1000);
         }
      } catch (MqttException e) {
         String mqttErrorPrefixWithDelim = MQTT_PREFIX + PREFIX_DELIMITER;
         firePropertyChange(REPORT_ERROR_LABEL, null, mqttErrorPrefixWithDelim +
               e.getMessage());
         log.warn("Unable to connect to broker --" + e.getMessage());
         Thread.currentThread().interrupt();
      } catch (InterruptedException e) {
         String mqttErrorPrefixWithDelim = MQTT_PREFIX + PREFIX_DELIMITER;
         firePropertyChange(REPORT_ERROR_LABEL, null, mqttErrorPrefixWithDelim +
               e.getMessage());
         log.warn("Thread was interrupted", e);
         Thread.currentThread().interrupt();
      }
   }

   @Override
   public void connectionLost(Throwable throwable) {
      log.warn("Connection lost: " + throwable.getMessage());
   }

   @Override
   public void messageArrived(String s, MqttMessage mqttMessage) {
      firePropertyChange(CLIENT_PROPERTY_LABEL, null, topicAndPrefixPairs.get(s) +
            PREFIX_DELIMITER + mqttMessage);
      log.debug("Message Arrived. Topic: " + s +
            " Message: " + new String(mqttMessage.getPayload()));
   }

   @Override
   public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

   }

   public void stopSubscriber() {
      running = false;
   }
}
