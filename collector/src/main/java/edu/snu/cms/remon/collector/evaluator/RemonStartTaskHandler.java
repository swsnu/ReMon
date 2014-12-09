package edu.snu.cms.remon.collector.evaluator;

import edu.snu.cms.remon.collector.EventType;
import org.apache.reef.task.events.TaskStart;
import org.apache.reef.wake.EventHandler;

import javax.inject.Inject;

/**
 * Log event for task start
 */
public class RemonStartTaskHandler implements EventHandler<TaskStart> {
  private final RemonLogger logger;

  @Inject
  public RemonStartTaskHandler(final RemonLogger logger) {
    this.logger = logger;
  }

  @Override
  public void onNext(TaskStart task) {
    final long timestamp = System.currentTimeMillis();
    logger.event("Task", EventType.START);
  }
}
