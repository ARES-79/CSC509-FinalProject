package emotivLib;

import mqttLib.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Main class to run the Emotiv WebSocket client.
 *
 *  @author javiersgs
 *  @author Andrew Estrada
 *  @author Sean Sponsler
 *  @author Xiuyuan Qiu
 *  @version 0.1
 */
public class EmotivServer implements Runnable {
    private final EmotivMQTTDelegate mqttDelegate;
    private final Logger logger = LoggerFactory.getLogger(EmotivServer.class);

    public EmotivServer(String broker, String clientId, String topic, Encoder encoder) {
        mqttDelegate = new EmotivMQTTDelegate(broker, clientId, topic, encoder);
    }

    @Override
    public void run() {
        try {
            EmotivLauncherDelegate delegate = new EmotivLauncherDelegate();
            URI uri = new URI("wss://localhost:6868");
            EmotivSocket ws = new EmotivSocket(uri, delegate, mqttDelegate);
            ws.connect();
        } catch (Exception e) {
            logger.error("Emotiv Server issue:{}", e.getMessage());
        }
    }

}