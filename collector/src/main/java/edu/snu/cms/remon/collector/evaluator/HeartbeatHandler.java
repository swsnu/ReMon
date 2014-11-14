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
  private static final String SOURCE_ID = "SourceId";

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
    final Runtime runtime = Runtime.getRuntime();
    values.add(new Metric(SOURCE_ID, "EvaluatorMaxMemory", System.currentTimeMillis(), (double)runtime.maxMemory()));
    values.add(new Metric(SOURCE_ID, "EvaluatorUsedMemory", System.currentTimeMillis(), (double)runtime.totalMemory() - runtime.freeMemory()));

    LOG.log(Level.INFO, "The metric list has sent to the Driver. Size : {0}", values.size());
    final TaskMessage msg =  TaskMessage.from(SOURCE_ID,
      new Codec().encode(values));
    values.clear();

    return Optional.of(msg);
  }
}
