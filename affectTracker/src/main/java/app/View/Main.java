package app.View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import affectTracker.TheSubscriberMQTT;
import app.Controller.MQTTMouseServer;
import app.Controller.MainController;
import app.Model.Blackboard;
import app.Model.MouseDataEncoder;
import app.Model.RawDataProcessor;
import app.Model.ViewDataProcessor;
import headSimulatorOneLibrary.Encoder;
import test.EmotivServer;
import test.MQTTEmotionServer;

/**
 * The {@code Main} class serves as the entry point for the Eye Tracking & Emotion Hub application.
 * It sets up the main window, initializes the user interface components, and starts the necessary threads
 * for data retrieval, processing, and visualization.
 * <p>
 * The application displays a user interface with a panel for adjusting preferences, a draw panel for
 * visualizing circles representing emotion and eye-tracking data, and a key panel explaining the color-coded emotions.
 * <p>
 * Main also acts as the default factory for necessary components.
 * <p>
 * If run with the "-test" flag, this class will also start the test servers for emotion and eye-tracking data.
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 */
public class Main extends JFrame {
	private static final String TESTING_FLAG = "-test";
	private TheSubscriberMQTT mqttSubscriber = null;
	//private TheSubscriber emotionSubscriber = null;
	private final DrawPanel drawPanel;
	
	public Main() {
		setLayout(new BorderLayout());
		//menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu actionsMenu = new JMenu("Actions");
		JMenuItem start = new JMenuItem("Start");
		JMenuItem stop = new JMenuItem("Stop");
		menuBar.add(actionsMenu);
		actionsMenu.add(start);
		actionsMenu.add(stop);
		setJMenuBar(menuBar);

		//panels
		drawPanel = new DrawPanel();
		drawPanel.setPreferredSize(new Dimension(1000, 1000));
		add(drawPanel, BorderLayout.CENTER);

		PreferencePanel preferencePanel = new PreferencePanel();
		add(preferencePanel, BorderLayout.NORTH);
		ColorKeyPanel colorKeyPanel = new ColorKeyPanel();
		colorKeyPanel.setPreferredSize(new Dimension(200, 1000));
		add(colorKeyPanel, BorderLayout.EAST);

		//controllers
		MainController controller = new MainController(this);
		start.addActionListener(controller);
		stop.addActionListener(controller);

		//Adding Blackboard Listeners
		Blackboard.getInstance().addPropertyChangeListener(Blackboard.EYE_DATA_LABEL, controller);
		Blackboard.getInstance().addPropertyChangeListener(Blackboard.EMOTION_DATA_LABEL, controller);
		Blackboard.getInstance().addPropertyChangeListener(Blackboard.PROPERTY_NAME_VIEW_DATA, drawPanel);

		//Starting Threads
		Thread dataProcessor = new Thread(new RawDataProcessor());
		Thread dpDelegate = new Thread(new ViewDataProcessor());

		dataProcessor.start();
		dpDelegate.start();

	}
	
	public void connectClients() {
		cleanUpThreads();

		Encoder mouseDataEncoder = new MouseDataEncoder();
		//TODO: change to getting info from blackboard so it can be changed by user
		// may end up needing to move this to connectClients()
		MQTTMouseServer mqttServer = new MQTTMouseServer(Blackboard.getInstance().getMqttBroker(),
				"MouseDataPublisher",
				"app/SimulatedEyeData", mouseDataEncoder);
		drawPanel.addMouseMotionListener(mqttServer);

		HashMap<String, String> topicsAndPrefixes = new HashMap<>();
		topicsAndPrefixes.put("app/SimulatedEyeData", Blackboard.EYE_DATA_LABEL);
		topicsAndPrefixes.put("app/SimulatedEmotionData", Blackboard.EMOTION_DATA_LABEL);

		mqttSubscriber = new TheSubscriberMQTT(Blackboard.getInstance().getMqttBroker(), "readingHub",
				topicsAndPrefixes, Blackboard.getInstance());

		Thread mouseDataServer = new Thread(mqttServer);
		mouseDataServer.start();
		Thread mqttSubscriberThread = new Thread(mqttSubscriber);
		mqttSubscriberThread.start();
	}
	
	public void cleanUpThreads() {
		if (mqttSubscriber != null ){
			mqttSubscriber.stopSubscriber();
			mqttSubscriber = null;
		}
	}
	
	private void startServerThreads() {
		System.out.println("Starting test servers.");

//		MQTTEmotionServer emotionServer = new MQTTEmotionServer(Blackboard.getInstance().getMqttBroker(),
//				"MQTTEmotionServer", "app/SimulatedEmotionData", message -> message);
//		Thread emotionDataThread = new Thread(emotionServer);
//		emotionDataThread.start();

		EmotivServer emotivServer = new EmotivServer(Blackboard.getInstance().getMqttBroker(),
				"MQTTEmotionServer", "app/SimulatedEmotionData", message -> message);
		Thread emotivDataThread = new Thread(emotivServer);
		emotivDataThread.start();
	}

	public static void main(String[] args) {
		Main window = new Main();
		window.setTitle ("Eye Tracking & Emotion Hub");
		window.setSize (1024, 768);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (args.length > 0 && args[0].equals(TESTING_FLAG)) {
			System.out.println(args[0]);
			window.startServerThreads();
		}
	}

}
