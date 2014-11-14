package edu.snu.cms.remon.collector.evaluator;

import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Metric;
import org.apache.reef.task.TaskMessage;
import org.apache.reef.task.TaskMessageSource;
import org.apache.reef.util.Optional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatHandler implements TaskMessageSource {
  private static final Logger LOG = Logger.getLogger(HeartbeatHandler.class.getName());
  private List<Metric> values = new ArrayList<>();

  @Inject
  public HeartbeatHandler() {
  }

  /**
   * Invoked when a Task is alarmed to send a heartbeat message.
   * It writes the message with collected values.
   * @return
   */
  @Override
  public Optional<TaskMessage> getMessage() {
    // TODO Move those to the right place.
    final Runtime runtime = Runtime.getRuntime();
    values.add(new Metric("SourceId", "EvaluatorMaxMemory", System.currentTimeMillis(), (double)runtime.maxMemory()));
    values.add(new Metric("SourceId", "EvaluatorUsedMemory", System.currentTimeMillis(), (double)runtime.totalMemory() - runtime.freeMemory()));

    LOG.log(Level.INFO, "The metric list has sent to the Driver. Size : {0}", values.size());
    final TaskMessage msg =  TaskMessage.from(this.toString(),
      new Codec().encode(values));
    values.clear();

    return Optional.of(msg);
  }
}
