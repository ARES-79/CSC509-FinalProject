package mqttLib;

public interface Encoder {

    String encodeMessageForMQTT(String message);
}
