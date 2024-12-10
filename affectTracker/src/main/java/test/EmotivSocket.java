package test;

import java.net.URI;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * EmotivSocket is a WebSocket client that connects to the Emotiv server.
 * It is used to send requests to the Emotiv server and receive responses.
 *
 *  @author javiersgs
 *  @version 0.1
 */
public class EmotivSocket extends WebSocketClient {

    private EmotivDelegate delegate;
    private MQTTHandler mqttHandler;

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
    };

    public EmotivSocket(URI serverURI, EmotivDelegate delegate, MQTTHandler mqttHandler) throws Exception {
        super(serverURI);
        this.delegate = delegate;
        this.mqttHandler = mqttHandler;
        // Disable SSL certificate validation to allow self-signed certificates
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        // Connect to Emotiv server using secure WebSocket protocol
        setSocket(sc.getSocketFactory().createSocket());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to Emotiv server: " + getURI());
        delegate.handle (0, null, this);
    }

    public Boolean handleServerEx(JSONObject res) {
        if (res.has("error")) {
            JSONObject err = res.getJSONObject("error");
            System.out.println("Server Error: " + err.getString("message"));
        } else if (res.has("warning")) {
            JSONObject warn = res.getJSONObject("warning");
            System.out.println("Server Warning: " + warn);
        } else {
            return false;
        }
        return true;
    }
    @Override
    public void onMessage(String message) {
        System.out.println("Received message from Emotiv server.");
        JSONObject response = new JSONObject(message);
        Boolean serverExceptionHappened = handleServerEx(response);
        if (!serverExceptionHappened) {
            if (!delegate.isSubscribed()) {
                int id = response.getInt("id");
                Object result = response.get("result");
                delegate.handle (id, result, this);
            } else {
                float time = new JSONObject(message).getFloat("time");
                JSONObject object = new JSONObject(message);
                JSONArray array = null;
                // "met" is for emotion, "dev" is for dev mode, "fac" is for facial gestures
                if (object.has("fac")) {
                    array = object.getJSONArray("fac");
                } else if (object.has("dev")) {
                    array = object.getJSONArray("dev");
                } else if (object.has("met")) {
                    array = object.getJSONArray("met");
                    mqttHandler.handleEmotions(array);
                }
                System.out.println(time + " :: " + array);
            }
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with code " + code + " and reason " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error: " + ex);
    }

}