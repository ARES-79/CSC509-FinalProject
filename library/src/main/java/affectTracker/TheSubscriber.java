package affectTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * The {@code TheSubscriber} class is responsible for connecting to a specified
 * IP address and port,
 * receiving data from the server, and sending it to a destination with a
 * specified prefix for parsing.
 * <p>
 * A connection is attempted during the constructor call to ensure the
 * connection exists before it is run
 * as a thread. This class listens for data from a server, attaches a prefix,
 * and forwards the data to a
 * designated listener for further processing.
 * </p>
 * <p>
 * When run as a thread, it continuously reads data from the server, prefixes
 * it, and notifies its listeners.
 * </p>
 * <p>
 * Code Metrics:
 * - Number of Methods: 6
 * - Lines of Code (LOC): 93
 * - Cyclomatic Complexity: 5 (due to try-catch and loop)
 * - Number of Conditional Branches: 4 (in `run` and `stopSubscriber` methods)
 * - Number of Loops: 1 (in `run` method)
 * </p>
 *
 * @author Andrew Estrada
 * @author Sean Sponsler
 * @author Xiuyuan Qiu
 * @version 1.0
 */
public class TheSubscriber extends PropertyChangeSupport implements Runnable {

   private final Logger log = LoggerFactory.getLogger(TheSubscriber.class.getName());
   private final DataInputStream inputStream;
   private final String dataPrefixWithDelim;
   public static final String CLIENT_PROPERTY_LABEL = "addClientData";
   public static final String REPORT_ERROR_LABEL = "reportSubscriberError";
   private static final String PREFIX_DELIMITER = "~";
   private boolean running = true;

   /**
    * Constructs a {@code TheSubscriber} instance that connects to the given IP
    * address and port
    * and initializes a property change listener.
    *
    * @param ip_host    The IP address of the server to connect to.
    * @param ip_port    The port number of the server.
    * @param dataPrefix The prefix to attach to received data.
    * @param listener   A listener to handle property change events.
    * @throws IOException If an error occurs while establishing the connection.
    */
   public TheSubscriber(String ip_host, int ip_port, String dataPrefix, PropertyChangeListener listener)
         throws IOException {
      super(new Object());
      this.dataPrefixWithDelim = dataPrefix + PREFIX_DELIMITER;
      try {
         Socket socket = new Socket(ip_host, ip_port);
         inputStream = new DataInputStream(socket.getInputStream());
         this.addPropertyChangeListener(CLIENT_PROPERTY_LABEL, listener);
         this.addPropertyChangeListener(REPORT_ERROR_LABEL, listener);
      } catch (IOException e) {
         log.warn("Unable to connect to server --" + e.getMessage());
         throw e;
      }
   }

   /**
    * Runs the subscriber thread, continuously reading data from the server
    * and forwarding it to listeners with the attached prefix.
    */
   @Override
   public void run() {
      try {
         while (running) {
            long startTime = System.currentTimeMillis();
            String str = inputStream.readUTF();
            firePropertyChange(CLIENT_PROPERTY_LABEL, null, dataPrefixWithDelim + str);
            long endTime = System.currentTimeMillis();
            log.info("Received data: " + str + " in " + (endTime - startTime) + "ms");
         }
      } catch (IOException ex) {
         if (running) {
            firePropertyChange(REPORT_ERROR_LABEL, null, dataPrefixWithDelim + ex.getMessage());
         }
         log.warn("Error with server connection - " + ex.getMessage());
      }
   }

   /**
    * Stops the subscriber and closes the input stream.
    */
   public void stopSubscriber() {
      try {
         log.debug("Stopping the Subscriber");
         inputStream.close();
      } catch (IOException e) {
         log.error("Error closing stream - " + e.getMessage());
      }
      running = false;
   }
}
