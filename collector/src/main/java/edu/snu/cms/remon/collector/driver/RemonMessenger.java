package edu.snu.cms.remon.collector.driver;

import edu.snu.cms.remon.collector.*;
import org.apache.reef.driver.task.TaskMessage;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.webserver.ReefEventStateManager;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemonMessenger implements EventHandler<TaskMessage> {
  private static final Logger LOG = Logger.getLogger(RemonMessenger.class.getName());

  private WebSocketClient client;
  private SimpleEchoSocket socket;
  private final String monitorAddress;
  private final ReefEventStateManager reefStateManager;
  private final String appId = "KMeans-"+System.currentTimeMillis();

  @Inject
  public RemonMessenger(final ReefEventStateManager reefStateManager) {
    this.monitorAddress = "ws://remon-client.herokuapp.com:80/websocket";
    this.reefStateManager = reefStateManager;
  }

  /**
   * Invoked when a heartbeat message arrives from a Task to the Driver.
   * Make a connection to Monitor and send the data.
   * @param value an event
   */
  @Override
  public void onNext(org.apache.reef.driver.task.TaskMessage value) {
    final Data data = new Codec().decode(value.get());

    // Add the number of evaluators as a metric
/*    metricList.add(new Metric(reefStateManager.getDriverEndpointIdentifier(), "NumEval", System.currentTimeMillis(), (double)reefStateManager.getEvaluators().size()));
    final Set<String> evalIds = reefStateManager.getEvaluators().keySet();
    for (String evalId : evalIds) {
      // Add memory size of each evaluator as a metric
      final long mem = reefStateManager.getEvaluators().get(evalId).getMemory();
      metricList.add(new Metric(reefStateManager.getDriverEndpointIdentifier(), "EvalMem" + "@" + evalId, System.currentTimeMillis(), (double)mem));
    }
    */

    // TODO Specify App1 to be the applicationId of this app
    //final Metrics metrics = new Metrics(appId, metricList);
    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
/*
    try {
      MetricSerializer.toStream(metrics, stream);
    } catch (IOException e) {
      e.printStackTrace();
    }
*/
    ByteBuffer buf = ByteBuffer.wrap(stream.toByteArray());
    client = new WebSocketClient();
    socket = new SimpleEchoSocket(buf);

    try {
      client.start();
      URI echoUri = new URI(monitorAddress);
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      client.connect(socket, echoUri, request);
      LOG.log(Level.INFO, "Connecting to : {0}", echoUri);
      socket.awaitClose(1, TimeUnit.SECONDS);
      LOG.log(Level.INFO, "Connecting to : {0}", echoUri);
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