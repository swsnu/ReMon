package edu.snu.cms.remon.collector.evaluator;

import edu.snu.cms.remon.collector.*;
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
  private List<Metric> metrics = new ArrayList<>();
  private List<Message> messages = new ArrayList<>();
  private List<Event> events = new ArrayList<>();
  private final String taskId;

  @Inject
  public RemonLogger(@Parameter(TaskConfigurationOptions.Identifier.class) String taskId) {
    this.taskId = taskId;
  }

  private void addMetric(final String tag, final double value) {
    final long time = System.currentTimeMillis();
    final Metric newMetric = Metric.newBuilder().setSourceId(taskId).setTag(tag).setTime(time).setValue(value).build();
    metrics.add(newMetric);
  }

  private void addMessage(final String level, final String msg) {
    final long time = System.currentTimeMillis();
    final Message newMessage = Message.newBuilder().setSourceId(taskId).setLevel(level).setTime(time).setMessage(msg).build();
    messages.add(newMessage);
  }

  private void addEvent(final String tag, final EventType type) {
    final long time = System.currentTimeMillis();
    final Event newEvent = Event.newBuilder().setSourceId(taskId).setTag(tag).setTime(time).setType(type).build();
    events.add(newEvent);
  }

  public void start(final String tag) {
    event(tag, EventType.START);
  }

  public void end(final String tag) {
    event(tag, EventType.END);
  }

  public void value(final String tag, final double value) {
    addMetric(tag, value);
  }

  public void log(final Level level, final String msg) {
    addMessage(level.getName(), msg);
  }

  public void event(final String tag, final EventType type) {
    addEvent(tag, type);
  }

  /**
   * Invoked when a Task is alarmed to send a heartbeat message.
   * It writes the message with collected values.
   * @return
   */
  @Override
  public Optional<TaskMessage> getMessage() {
    // TODO : we should retrieve APPID automatic-manner. currently "K-means" is hardcoded.
    final Data data = new Data("insert", "K-means", metrics, messages, events);
    final TaskMessage msg = TaskMessage.from(MSG_SOURCE, new Codec().encode(data));
    metrics.clear();
    messages.clear();
    return Optional.of(msg);
  }
}
