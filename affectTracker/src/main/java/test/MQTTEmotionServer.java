package test;

import headSimulatorOneLibrary.Encoder;
import headSimulatorOneLibrary.ThePublisherMQTT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MQTTEmotionServer implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(MQTTEmotionServer.class);

    private final ThePublisherMQTT mqttPublisher;

    private final String topic;

    public MQTTEmotionServer(String broker, String clientId, String topic, Encoder encoder) {
        this.topic = topic;
        mqttPublisher = new ThePublisherMQTT(broker,  clientId, encoder);
    }

    @Override
    public void run() {
        try{
            logger.debug("MQTTEmotionServer Started");
            mqttPublisher.connect();
            Random random = new Random();
            if (mqttPublisher.isConnected()) {
                while (true) {
                    float v1 = random.nextFloat();
                    float v2 = random.nextFloat();
                    float v3 = random.nextFloat();
                    float v4 = random.nextFloat();
                    float v5 = random.nextFloat();
                    String emotionData = String.format("%f, %f, %f, %f, %f", v1, v2, v3, v4, v5);
                    mqttPublisher.publish(topic, emotionData);
                    Thread.sleep(200);
                }
            }
        } catch (InterruptedException e) {
            logger.error(" MQTTServer was interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            mqttPublisher.disconnect();
        }
    }
}
