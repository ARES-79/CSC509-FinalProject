package emotivLib;

import mqttLib.Encoder;
import mqttLib.ThePublisherMQTT;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * Class that provides handleEmotions() to read a JSONArray of Emotiv Emotions and publish it with mqttPublisher
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * */
public class EmotivMQTTDelegate {
    private final ThePublisherMQTT mqttPublisher;
    private final String topic;
    private final Logger logger = LoggerFactory.getLogger(EmotivMQTTDelegate.class);

    public EmotivMQTTDelegate(String broker, String clientId, String topic, Encoder encoder) {
        this.topic = topic;
        mqttPublisher = new ThePublisherMQTT(broker,  clientId, encoder);
        mqttPublisher.connect();
    }

    // 6 emotions; Attention Engagement Excitement Interest Relaxation Stress
    private double[] parseEmotions(JSONArray emotions) {
        int EMOTION_CT = 6;
        double[] emotionTable = new double[EMOTION_CT];
        if (emotions.length() != EMOTION_CT * 2 + 1) {
            logger.warn("parseEmotions got array size: {}, expected {}, can't interpret",
                        emotions.length(), EMOTION_CT * 2 + 1);
            return null;
        }
        for (int i = 0; i < EMOTION_CT; i++) {
            int isActiveIdx = 2 * i;
            int valueIdx = 2 * i + 1;
            // for some reason the third emotion has 2 values, so we have to increment when i >= 2
            // [true,0.788972,true,0.768653,true,0.81132,0,true,0.772702,true,0.787863,true,0.764932]
            if (i >= 3) {
                isActiveIdx++;
                valueIdx++;
            }
            boolean isActive = emotions.getBoolean(isActiveIdx);
            if (isActive) {
                emotionTable[i] = emotions.getDouble(valueIdx);
            } else {
                // inactive emotion
                emotionTable[i] = -1;
            }
        }
        return emotionTable;

    }
    public void publishEmotions(JSONArray emotions) {
        if (mqttPublisher.isConnected()) {
            double[] emotionVals = parseEmotions(emotions);
            if (emotionVals != null) {
                String emotionDataAsString = Arrays.stream(emotionVals)
                        .mapToObj(String::valueOf) // Convert each float to String
                        .collect(Collectors.joining(", "));
                mqttPublisher.publish(topic, emotionDataAsString);
            }
        }
    }
}
