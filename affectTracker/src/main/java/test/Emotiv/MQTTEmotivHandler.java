package test.Emotiv;

import headSimulatorOneLibrary.Encoder;
import headSimulatorOneLibrary.ThePublisherMQTT;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * Class that provides handleEmotions() to read a JSONArray of Emotiv Emotions and publish it with mqttPublisher
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * */
public class MQTTEmotivHandler {
    private final ThePublisherMQTT mqttPublisher;
    private final String topic;

    public MQTTEmotivHandler(String broker, String clientId, String topic, Encoder encoder) {
        this.topic = topic;
        mqttPublisher = new ThePublisherMQTT(broker,  clientId, encoder);
        mqttPublisher.connect();
    }

    // ATTENTION ENGAGEMENT EXCITEMENT INTEREST RELAXATION STRESS
    private double[] parseEmotions(JSONArray emotions) {
        int EMOTION_CT = 6;
        double[] emotionTable = new double[EMOTION_CT];
        if (emotions.length() != EMOTION_CT * 2 + 1) {
            System.out.println("Invalid emotion count: " + emotions.length());
            return null;
        }
        for (int i = 0; i < EMOTION_CT; i++) {
            int isActiveIdx = 2 * i;
            int valueIdx = 2 * i + 1;
            if (i >= 3) {
                isActiveIdx++;
                valueIdx++;
            }
            Boolean isActive = emotions.getBoolean(isActiveIdx);
            if (isActive) {
                emotionTable[i] = emotions.getDouble(valueIdx);
            } else {
                emotionTable[i] = 0;
            }
        }
        return emotionTable;

    }
    public void handleEmotions(JSONArray emotions) {
        if (mqttPublisher.isConnected()) {
            double[] emotionVals = parseEmotions(emotions);
            String emotionData = Arrays.stream(emotionVals)
                    .mapToObj(String::valueOf) // Convert each float to String
                    .collect(Collectors.joining(", "));
            mqttPublisher.publish(topic, emotionData);
        }
    }
}
