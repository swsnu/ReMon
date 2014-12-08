package edu.snu.cms.remon.collector.evaluator;

import org.apache.reef.task.HeartBeatTriggerManager;
import org.apache.reef.task.events.TaskStop;
import org.apache.reef.wake.EventHandler;

import javax.inject.Inject;

/**
 * Log event for task stop
 */
public class RemonStopTaskHandler implements EventHandler<TaskStop> {
  private final RemonLogger logger;
  private final HeartBeatTriggerManager heartBeatTriggerManager;

  @Inject
  public RemonStopTaskHandler(final RemonLogger logger,
                              final HeartBeatTriggerManager heartBeatTriggerManager) {
    this.logger = logger;
    this.heartBeatTriggerManager = heartBeatTriggerManager;
  }

  @Override
  public void onNext(TaskStop task) {
    final long timestamp = System.currentTimeMillis();
    // TODO This is just log "Stop" message. Revisit when new API is implemented for Task Start/Complete
    logger.value("Stop " + task.getId(), timestamp);
    heartBeatTriggerManager.triggerHeartBeat();
  }
}
