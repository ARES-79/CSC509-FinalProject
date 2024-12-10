package test.Emotiv;

import headSimulatorOneLibrary.Encoder;

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
            System.out.println("Emotiv Server issue:" + e.getMessage());
        }
    }

}