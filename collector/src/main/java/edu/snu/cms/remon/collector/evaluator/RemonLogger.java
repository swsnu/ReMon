package edu.snu.cms.remon.collector.evaluator;

import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Metric;
import org.apache.reef.task.TaskMessage;
import org.apache.reef.task.TaskMessageSource;
import org.apache.reef.util.Optional;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemonLogger implements TaskMessageSource {
  private static RemonLogger logger;
  public static final String SOURCE_ID = "remon";
  private static final Logger LOG = Logger.getLogger(RemonLogger.class.getName());
  // TODO Which would be a good value for Metric.Source?
  private List<Metric> logs;

  @Inject
  public RemonLogger() {
  }

  public static RemonLogger getLogger() {
    if (logger == null) {
      logger = new RemonLogger();
    }
    return logger;
  }

  public void log(String str) {
    LOG.log(Level.SEVERE, str);
    final String sourceID = "";
    final String tag = "log";
    final long time = System.currentTimeMillis();
    final Metric newMetric = Metric.newBuilder().setSourceId(sourceID).setTag(tag).setTime(time).build();
    logs.add(newMetric);
  }

  /**
   * Invoked when a Task is alarmed to send a heartbeat message.
   * It writes the message with collected values.
   * @return
   */
  @Override
  public Optional<TaskMessage> getMessage() {
    // TODO Define a codec and encode the logs with it
    final TaskMessage msg =  TaskMessage.from("remon", Codec.getCodec().encode(logs));
    logs.clear();
    return Optional.of(msg);
  }
}
