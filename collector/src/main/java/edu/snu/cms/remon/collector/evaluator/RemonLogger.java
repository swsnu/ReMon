package edu.snu.cms.remon.collector.evaluator;

import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Message;
import edu.snu.cms.remon.collector.Metric;
import org.apache.reef.task.TaskMessage;
import org.apache.reef.task.TaskMessageSource;
import org.apache.reef.util.Optional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemonLogger implements TaskMessageSource {
  public static final String MSG_SOURCE_ID = "remon";
  private static final Logger LOG = Logger.getLogger(RemonLogger.class.getName());
  private static final String LOG_SOURCE_ID = "ComputeTask";
  // TODO Which would be a good value for Metric.Source?
  private List<Metric> metrics = new ArrayList<>();
  private List<Message> messages = new ArrayList<>();

  @Inject
  public RemonLogger() {
  }

  private void addMetric(final String tag, final double value) {
    final String sourceId = LOG_SOURCE_ID;
    final long time = System.currentTimeMillis();
    final Metric newMetric = Metric.newBuilder().setSourceId(sourceId).setTag(tag).setTime(time).setValue(value).build();
    metrics.add(newMetric);
  }
  private void addMessage(final String level, final String msg) {
    final String sourceId = LOG_SOURCE_ID;
    final long time = System.currentTimeMillis();
    final Message newMessage = Message.newBuilder().setSourceId(sourceId).setLevel(level).setTime(time).setMessage(msg).build();
    messages.add(newMessage);
  }

  public void log(final String tag, final double value) {
    addMetric(tag, value);
  }

  public void log(final String level, final String msg) {
    addMessage(level, msg);
  }

  /**
   * Invoked when a Task is alarmed to send a heartbeat message.
   * It writes the message with collected values.
   * @return
   */
  @Override
  public Optional<TaskMessage> getMessage() {
    // TODO Define a codec and encode the logs with it
    final TaskMessage msg = TaskMessage.from("remon", new Codec().encode(null));
    metrics.clear();
    messages.clear();
    return Optional.of(msg);
  }
}
