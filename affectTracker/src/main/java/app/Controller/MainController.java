package app.Controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.Model.Blackboard;
import app.View.Main;

/**
 * The {@code MainController} class serves as an event handler for UI actions,
 * implementing the {@link ActionListener} interface. It handles "Start" and
 * "Stop" actions
 * by interacting with the {@link Blackboard} to start and stop data retrieval.
 * <p>
 * This controller listens for user actions (such as button clicks) and triggers
 * the appropriate
 * data retrieval methods in the {@code Blackboard} instance based on the action
 * command.
 * 
 * <p>
 * Code Metrics:
 * - Number of Methods: 6
 * - Lines of Code (LOC): 92
 * - Cyclomatic Complexity: 6 (due to `switch` statements in actionPerformed and
 * propertyChange)
 * - Number of Conditional Branches: 5 (if conditions inside the
 * `actionPerformed` and `propertyChange` methods)
 * - Number of Loops: 0
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class MainController implements ActionListener, PropertyChangeListener {

   private static final Logger controllerLog = LoggerFactory.getLogger(MainController.class.getName());
   private final Main parent;
   private static final String CONNATTEMPT_STRING = "Connection attempted with:\n%s";
   private static final String CONNDISCONNECT_STRING = "Stop Pressed. Disconnecting...";
   private static final String CONNFAIL_STRING = "Unable to connect to %S server. \n" +
         "Please check that the server is running and the IP address is correct.";
   private static final String MQTTFAIL_STRING = "Issue with MQTT Broker\n%s";

   public MainController(Main parent) {
      this.parent = parent;
   }

   /**
    * Handles action events (button clicks) for starting and stopping data
    * retrieval.
    * 
    * @param e the action event triggered by the user
    */
   @Override
   public void actionPerformed(ActionEvent e) {
      switch (e.getActionCommand()) {
         case ("Start") -> {
            controllerLog.info(String.format(CONNATTEMPT_STRING,
                  Blackboard.getInstance().getFormattedConnectionSettings()));
            Blackboard.getInstance().startedProcessing();
            parent.connectClients();
         }
         case ("Stop") -> {
            controllerLog.info(CONNDISCONNECT_STRING);
            Blackboard.getInstance().stoppedProcessing();
            parent.cleanUpThreads();
         }
      }
   }

   /**
    * Listens for property changes from the Blackboard and triggers appropriate
    * actions based on the event.
    * 
    * @param evt the property change event containing the updated data
    */
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case Blackboard.EYE_DATA_LABEL -> {
            parent.cleanUpThreads();
            createConnectionErrorPopUp(String.format(CONNFAIL_STRING, "Emotion"), evt.getNewValue().toString());
         }
         case Blackboard.EMOTION_DATA_LABEL -> {
            createConnectionErrorPopUp(String.format(CONNFAIL_STRING, "Eye Tracking"), evt.getNewValue().toString());
         }
         case Blackboard.MQTTBROKER_ERROR ->
            JOptionPane.showMessageDialog(parent, String.format(MQTTFAIL_STRING, evt.getNewValue().toString()));
      }
   }

   /**
    * Displays a popup window with the connection error message.
    * 
    * @param main_message  the main error message
    * @param error_message the specific error message
    */
   public void createConnectionErrorPopUp(String main_message, String error_message) {
      JOptionPane.showMessageDialog(parent,
            String.format("%s\n\n%s\nError: %s", main_message,
                  Blackboard.getInstance().getFormattedConnectionSettings(),
                  error_message));
   }

}
