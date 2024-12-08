package app.Controller;

import headSimulatorOneLibrary.Encoder;
import headSimulatorOneLibrary.ThePublisherMQTT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MQTTMouseServer implements MouseMotionListener, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MQTTMouseServer.class);

    private final ThePublisherMQTT mqttPublisher;

    private final String topic;

    public MQTTMouseServer(String broker, String clientId, String topic, Encoder encoder) {
        this.topic = topic;
        mqttPublisher = new ThePublisherMQTT(broker,  clientId, encoder);
    }
    @Override
    public void run() {
        try{
            mqttPublisher.connect();

            if (mqttPublisher.isConnected()) {
                //keep thread alive to wait for the mouse to be dragged
                synchronized (this) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            logger.error(" MQTTServer was interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            mqttPublisher.disconnect();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        String mouseData = String.format("%d %d", e.getX(), e.getY()) ;
        mqttPublisher.publish(topic, mouseData);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //do nothing
    }


}
