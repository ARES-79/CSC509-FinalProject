package emotivLib;

import java.net.URI;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EmotivSocket is a WebSocket client that connects to the emotivLib server.
 * It is used to send requests to the emotivLib server and receive responses.
 *
 *  @author javiersgs
 *  @author Andrew Estrada
 *  @author Sean Sponsler
 *  @author Xiuyuan Qiu
 *  @version 0.1
 */
public class EmotivSocket extends WebSocketClient {

    private final EmotivLauncherDelegate launcherDelegate;
    private final EmotivMQTTDelegate mqttDelegate;
    private final Logger logger = LoggerFactory.getLogger(EmotivSocket.class);

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

    public EmotivSocket(URI serverURI, EmotivLauncherDelegate launcherDelegate, EmotivMQTTDelegate mqttDelegate) throws Exception {
        super(serverURI);
        this.launcherDelegate = launcherDelegate;
        this.mqttDelegate = mqttDelegate;
        // Disable SSL certificate validation to allow self-signed certificates
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        // Connect to Emotiv server using secure WebSocket protocol
        setSocket(sc.getSocketFactory().createSocket());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Connected to Emotiv server: {}", getURI());
        launcherDelegate.handle (0, null, this);
    }

    public Boolean handleServerEx(JSONObject res) {
        if (res.has("error")) {
            JSONObject err = res.getJSONObject("error");
            logger.error("Launcher Error: {}: {}", err.getString("message"), err);
        } else if (res.has("warning")) {
            JSONObject warn = res.getJSONObject("warning");
            logger.warn("Launcher Warning: {}", warn);
        } else {
            return false;
        }
        return true;
    }
    @Override
    public void onMessage(String message) {
        logger.info("Received message from Emotiv server.");
        JSONObject response = new JSONObject(message);
        Boolean serverExceptionHappened = handleServerEx(response);
        if (!serverExceptionHappened) {
            if (!launcherDelegate.isSubscribed()) {
                int id = response.getInt("id");
                Object result = response.get("result");
                launcherDelegate.handle (id, result, this);
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
                    mqttDelegate.handleEmotions(array);
                }
                logger.info("{} :: {}", time, array);
            }
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Connection closed with code {} and reason {}", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        logger.error("Error: {}", String.valueOf(ex));
    }

}