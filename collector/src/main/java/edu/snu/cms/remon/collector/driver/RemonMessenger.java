package edu.snu.cms.remon.collector.driver;

import edu.snu.cms.remon.collector.*;
import edu.snu.cms.remon.collector.evaluator.RemonLogger;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemonMessenger implements EventHandler<TaskMessage> {
//  private static final Logger LOG = Logger.getLogger(RemonMessenger.class.getName());

  private final ReefEventStateManager reefStateManager;

  /**
   * When Driver gets the TaskMessages, RemonMessenger sends the message
   * to the Monitor server.
   * @param reefStateManager The reef state can be retrieved via this manager
   */
  @Inject
  public RemonMessenger(final ReefEventStateManager reefStateManager) {
    this.reefStateManager = reefStateManager;
  }

  /**
   * Invoked when a message from evaluators arrives to the Driver.
   * It filter to handle the messages only from RemonLogger.
   * After converting in an appropriate form, send the data to the RemonMonitor.
   * @param value an event
   */
  @Override
  public void onNext(TaskMessage value) {
    // Skip the message if the source id is not from RemonLogger
    if(!RemonLogger.SOURCE_ID.equals(value.getMessageSourceID())) {
      return;
    }
    // TODO Send the message to the Monitor server
  }
}

