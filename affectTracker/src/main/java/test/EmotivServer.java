package test;

import java.net.URI;

/**
 * Main class to run the Emotiv WebSocket client.
 *
 *  @author javiersgs
 *  @version 0.1
 */
public class EmotivServer {

    public static void main(String[] args) throws Exception {
        EmotivDelegate delegate = new EmotivDelegate();
        URI uri = new URI("wss://localhost:6868");
        EmotivSocket ws = new EmotivSocket(uri, delegate);
        ws.connect();
    }
}