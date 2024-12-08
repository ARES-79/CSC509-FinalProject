package headSimulatorOneLibrary;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the common implementation of the MQTT Publisher
 * It has implementations for connecting, confirming connection, disconnecting, and publishing data to a subscriber all using an MQTT Client
 * 
 * @author Samuel Fox Gar Kaplan
 * @author Javier Gonzalez-Sanchez
 * @author Luke Aitchison
 * @author Ethan Outangoun
 *
 * @version 2.0
 * 
 */



public class ThePublisherMQTT{

    private final String BROKER;
    private final String CLIENT_ID;
    private final Encoder encoder;
    private MqttClient client;
    private static final Logger logger = LoggerFactory.getLogger(ThePublisherMQTT.class);


    public ThePublisherMQTT(String broker, String clientId, Encoder encoder){

        this.BROKER = broker;
        this.CLIENT_ID = clientId;
        this.encoder = encoder;
        try {
            client = new MqttClient(BROKER, CLIENT_ID);
        } catch (MqttException e) {
            logger.error("Error in Publisher", e);
        }
    }

    public void connect(){
        try {
            client.connect();
            logger.info(CLIENT_ID + " connected to " + BROKER);
        } catch (MqttException e) {
            logger.error("Error in Publisher", e);
        }
    }

    public boolean isConnected(){
        return client.isConnected();
    }

    public void disconnect(){
        try {
            client.disconnect();
            System.out.println(CLIENT_ID + " disconnected from " + BROKER);
        } catch (MqttException e) {
            logger.error("Error in Publisher", e);
        }
    }

    public void publish(String topic, String content){
        try {
            String encodedContent = encoder.encodeMessageForMQTT(content);
            MqttMessage message= new MqttMessage(encodedContent.getBytes());
            message.setQos(2);

            logger.debug("Is publisher connected?: {}", client.isConnected());

            if (client.isConnected()) {
                client.publish(topic, message);
            }

            logger.info("Message published on " + topic + ": " + message);
        } catch (MqttException e) {
            logger.error("Error in Publisher", e);
        }
    }
}