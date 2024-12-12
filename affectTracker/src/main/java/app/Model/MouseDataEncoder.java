package app.Model;

import mqttLib.Encoder;

public class MouseDataEncoder implements Encoder {

    /**
     * Message expected to be of the format:  <code> "{x} {y}" </code>
     * where x and y are integer values corresponding to the coordinates of the mouse.
     * <p>
     * Encoded message to be of format:
     * <code>"{'gaze_point_on_display_area': ({x}, {y})}"</code>
     * to mimic the eye-tracking payload.
     * <p>
     * @see <a href=”https://stackoverflow.com/questions/60470644/tobii-eyetracker-with-python-unable-to-print-gaze-data”>StackOverFlow Post</a>
     *
     * @param message message containing mouse data
     * @return  message structured for sending to the MQTTBroker
     */
    @Override
    public String encodeMessageForMQTT(String message) {
        String[] coords = message.split(" ");
        return String.format("{'gaze_point_on_display_area': (%s, %s)}",
                coords[0], coords[1]) ;
    }
}
