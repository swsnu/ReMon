package edu.snu.cms.remon.collector.evaluator;

import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Metric;
import org.apache.reef.driver.task.TaskConfigurationOptions;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.task.TaskMessage;
import org.apache.reef.task.TaskMessageSource;
import org.apache.reef.util.Optional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemonLogger implements TaskMessageSource {
  public static final String MSG_SOURCE = "remon";
  private static final Logger LOG = Logger.getLogger(RemonLogger.class.getName());
  // TODO Which would be a good value for Metric.Source?
  private List<Metric> logs = new ArrayList<>();
  private final String taskId;

  @Inject
  public RemonLogger(@Parameter(TaskConfigurationOptions.Identifier.class) String taskId) {
    this.taskId = taskId;
  }

  private void addToLog(final String tag, final String str) {
    LOG.log(Level.SEVERE, str);
    final long time = System.currentTimeMillis();
    final Metric newMetric = Metric.newBuilder().setSourceId(taskId).setTag(tag).setTime(time).build();
    logs.add(newMetric);
  }

  public void log(final Level level, final String str) {
    addToLog(level.toString(), str);
  }

  public void value(final String tag, final double value) {
    LOG.log(Level.SEVERE, "new Value " + value + "is added");
    final long time = System.currentTimeMillis();
    final Metric newMetric = Metric.newBuilder().setSourceId(taskId).setTag(tag).setTime(time).setValue(value).build();
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
    final TaskMessage msg =  TaskMessage.from(MSG_SOURCE, new Codec().encode(new ArrayList(logs)));
    logs.clear();
    return Optional.of(msg);
  }
}
