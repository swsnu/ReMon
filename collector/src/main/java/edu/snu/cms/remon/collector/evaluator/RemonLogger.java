package edu.snu.cms.remon.collector.evaluator;

import org.apache.reef.task.TaskMessage;
import org.apache.reef.task.TaskMessageSource;
import org.apache.reef.util.Optional;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemonLogger implements TaskMessageSource {
  public static final String SOURCE_ID = "remon";
  private static final Logger LOG = Logger.getLogger(RemonLogger.class.getName());
  // TODO Define a data structure to be rendered in Monitor
  private List<Object> logs;

  @Inject
  public RemonLogger() {
  }

  public static RemonLogger getLogger() {
    return new RemonLogger();
  }

  public void log(String str) {
    LOG.log(Level.SEVERE, str);
    logs.add(str);
  }

  /**
   * Invoked when a Task is alarmed to send a heartbeat message.
   * It writes the message with collected values.
   * @return
   */
  @Override
  public Optional<TaskMessage> getMessage() {
    // TODO Define a codec and encode the logs with it
    final TaskMessage msg =  TaskMessage.from("remon", "encoded logs".getBytes());
    logs.clear();
    return Optional.of(msg);
  }
}
