package app.View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import mqttLib.TheSubscriberMQTT;
import app.Controller.MQTTMouseServer;
import app.Controller.MainController;
import app.Model.Blackboard;
import app.Model.MouseDataEncoder;
import app.Model.RawDataProcessor;
import app.Model.ViewDataProcessor;
import emotivLib.EmotivServer;
import mqttLib.Encoder;

/**
 * The {@code Main} class serves as the entry point for the Eye Tracking &
 * Emotion Hub application. It sets up the main window, initializes the user
 * interface components, and starts the necessary threads for data retrieval,
 * processing, and visualization.
 * 
 * <p>
 * This application displays a user interface with a panel for adjusting
 * preferences,
 * a draw panel for visualizing circles representing emotion and eye-tracking
 * data,
 * and a key panel explaining the color-coded emotions.
 * </p>
 * 
 * <p>
 * Main also acts as the default factory for necessary components.
 * </p>
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
public class Main extends JFrame {
   private TheSubscriberMQTT mqttSubscriber = null;
   private final DrawPanel drawPanel;

   /**
    * Initializes the main window and UI components, including the menu bar,
    * action buttons, and panels for displaying visual data.
    */
   public Main() {
      setLayout(new BorderLayout());

      // Menu bar
      JMenuBar menuBar = new JMenuBar();
      JMenu actionsMenu = new JMenu("Actions");
      JMenuItem start = new JMenuItem("Start");
      JMenuItem stop = new JMenuItem("Stop");
      JMenuItem preferencesMenuItem = new JMenuItem("Preferences");
      preferencesMenuItem.addActionListener(e -> openPreferencesWindow());
      menuBar.add(actionsMenu);
      actionsMenu.add(start);
      actionsMenu.add(stop);
      actionsMenu.add(preferencesMenuItem);
      setJMenuBar(menuBar);

      // Panels
      drawPanel = new DrawPanel();
      drawPanel.setPreferredSize(new Dimension(1000, 1000));
      add(drawPanel, BorderLayout.CENTER);

      ColorKeyPanel colorKeyPanel = new ColorKeyPanel();
      colorKeyPanel.setPreferredSize(new Dimension(200, 1000));
      add(colorKeyPanel, BorderLayout.EAST);

      // Controllers
      MainController controller = new MainController(this);
      start.addActionListener(controller);
      stop.addActionListener(controller);

      // Adding Blackboard Listeners
      Blackboard.getInstance().addPropertyChangeListener(Blackboard.EYE_DATA_LABEL, controller);
      Blackboard.getInstance().addPropertyChangeListener(Blackboard.EMOTION_DATA_LABEL, controller);
      Blackboard.getInstance().addPropertyChangeListener(Blackboard.PROPERTY_NAME_VIEW_DATA, drawPanel);

      // Starting Threads
      Thread dataProcessor = new Thread(new RawDataProcessor());
      Thread dpDelegate = new Thread(new ViewDataProcessor());

      dataProcessor.start();
      dpDelegate.start();
   }

   /**
    * Opens the preferences window where users can adjust settings.
    */
   private void openPreferencesWindow() {
      JFrame preferencesFrame = new JFrame("Preferences");
      preferencesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      preferencesFrame.setSize(600, 400); // Set size for the preferences window
      preferencesFrame.setLocationRelativeTo(this); // Center the window

      PreferencePanel preferencePanel = new PreferencePanel();
      preferencesFrame.add(preferencePanel);

      preferencesFrame.setVisible(true);
   }

   /**
    * Establishes MQTT connections and starts the necessary servers for data
    * retrieval.
    */
   public void connectClients() {
      cleanUpThreads();

      Encoder mouseDataEncoder = new MouseDataEncoder();
      MQTTMouseServer mqttServer = new MQTTMouseServer(Blackboard.getInstance().getMqttBroker(),
            "MouseDataPublisher",
            "app/SimulatedEyeData", mouseDataEncoder);
      drawPanel.addMouseMotionListener(mqttServer);

      HashMap<String, String> topicsAndPrefixes = new HashMap<>();

      topicsAndPrefixes.put(Blackboard.getInstance().getMqttEyeTopic(), Blackboard.EYE_DATA_LABEL);
      topicsAndPrefixes.put(Blackboard.getInstance().getMqttEmotionTopic(), Blackboard.EMOTION_DATA_LABEL);

      mqttSubscriber = new TheSubscriberMQTT(Blackboard.getInstance().getMqttBroker(), "readingHub",
            topicsAndPrefixes, Blackboard.getInstance());

      Thread mouseDataServer = new Thread(mqttServer);
      mouseDataServer.start();
      Thread mqttSubscriberThread = new Thread(mqttSubscriber);
      mqttSubscriberThread.start();
   }

   /**
    * Cleans up any active threads.
    */
   public void cleanUpThreads() {
      if (mqttSubscriber != null) {
         mqttSubscriber.stopSubscriber();
         mqttSubscriber = null;
      }
   }

   /**
    * Starts the server threads for testing purposes.
    */
   private void startServerThreads() {
      EmotivServer emotivServer = new EmotivServer(Blackboard.getInstance().getMqttBroker(),
            "MQTTEmotionServer", Blackboard.getInstance().getMqttEmotionTopic(), message -> message);
      Thread emotivDataThread = new Thread(emotivServer);
      emotivDataThread.start();
   }

   /**
    * The main entry point for the application. Initializes the main window
    * and starts the necessary threads for both data retrieval and visualization.
    * 
    * @param args Command-line arguments to specify testing or default behavior.
    */
   public static void main(String[] args) {
      Main window = new Main();
      window.setTitle("Eye Tracking & Emotion Hub");
      window.setSize(1024, 768);
      window.setLocationRelativeTo(null);
      window.setVisible(true);
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.startServerThreads();
   }
}
