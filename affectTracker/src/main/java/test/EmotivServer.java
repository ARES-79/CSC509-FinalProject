package test;

import headSimulatorOneLibrary.Encoder;
import headSimulatorOneLibrary.ThePublisherMQTT;
import org.eclipse.paho.client.mqttv3.MqttClient;

import java.net.URI;

/**
 * Main class to run the Emotiv WebSocket client.
 *
 *  @author javiersgs
 *  @version 0.1
 */
public class EmotivServer implements Runnable {
    private final MQTTHandler mqttHandler;

    public EmotivServer(String broker, String clientId, String topic, Encoder encoder) {
        mqttHandler = new MQTTHandler(broker, clientId, topic, encoder);
    }

    @Override
    public void run() {
        try {
            EmotivDelegate delegate = new EmotivDelegate();
            URI uri = new URI("wss://localhost:6868");
            EmotivSocket ws = new EmotivSocket(uri, delegate, mqttHandler);
            ws.connect();
        } catch (Exception e) {
            System.out.println("Emotiv Server issue:" + e.getMessage());
        }
    }

}