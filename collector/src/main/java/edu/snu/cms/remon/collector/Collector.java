package edu.snu.cms.remon.collector;

import org.apache.reef.tang.annotations.Name;
import org.apache.reef.tang.annotations.NamedParameter;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.task.TaskMessage;
import org.apache.reef.task.TaskMessageSource;
import org.apache.reef.util.Optional;
import org.apache.reef.wake.EventHandler;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collector class. This class provides an interface to register
 * the data to observe.
 * More interfaces are on progress to implement
 */
public final class Collector {
  @NamedParameter(doc = "The address of monitor server", short_name = "monitor_addr", default_value="ws://localhost:8080/websocket")
  public class MonitorAddress implements Name<String> {
  }

  private static final Logger LOG = Logger.getLogger(Collector.class.getName());

  public static List<Metric> values = new ArrayList<>();

  /*
   * TODO I think it is better to avoid create a number of Collector object. Instead we may want to keep it as a singleton.
   * But not sure yet this is a right approach.
   */
  private Collector() {
  }

  /**
   * Collects data when the size is up to 30 and
   * flushes the collection there reaches 30 items in the list.
   * @param tag
   * @param value
   */
  public synchronized static void putData(final String sourceId, final String tag, final double value) {
    final long time = System.currentTimeMillis();
    values.add(new Metric(sourceId, tag, time, value));
  }

  public static class heartbeatHandler implements TaskMessageSource {
    @Inject
    public heartbeatHandler() {
    }

    /**
     * Invoked when a Task is alarmed to send a heartbeat message.
     * It writes the message with collected values.
     * @return
     */
    @Override
    public Optional<TaskMessage> getMessage() {
      final TaskMessage msg =  TaskMessage.from(this.toString(),
        new Codec().encode(values));
      values.clear();
      return Optional.of(msg);
    }
  }

  public static class heartbeatBypassHandler implements EventHandler<org.apache.reef.driver.task.TaskMessage> {
    private WebSocketClient client;
    private SimpleEchoSocket socket;
    private final String monitorAddress;

    @Inject
    public heartbeatBypassHandler(@Parameter(Collector.MonitorAddress.class) String monitorAddress) {
      this.monitorAddress = monitorAddress;
    }

    /**
     * Invoked when a heartbeat message arrives from a Task to the Driver.
     * Make a connection to Monitor and send the data.
     * @param value an event
     */
    @Override
    public void onNext(org.apache.reef.driver.task.TaskMessage value) {
      // TODO Specify App1 to be the applicationId of this app
      final Metrics metrics = new Metrics("App1", new Codec().decode(value.get()));
      final ByteArrayOutputStream stream = new ByteArrayOutputStream();

      try {
        MetricSerializer.toStream(metrics, stream);
      } catch (IOException e) {
        e.printStackTrace();
      }

      ByteBuffer buf = ByteBuffer.wrap(stream.toByteArray());
      client = new WebSocketClient();
      socket = new SimpleEchoSocket(buf);

      try {
        client.start();
        URI echoUri = new URI(monitorAddress);
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        client.connect(socket, echoUri, request);
        LOG.log(Level.INFO, "Connecting to : {0}", echoUri);
        socket.awaitClose(120, TimeUnit.SECONDS);
      } catch (Throwable t) {
        t.printStackTrace();
      } finally {
        try {
          client.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
