package edu.snu.cms.remon.collector.evaluator;

import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Collector;
import org.apache.reef.task.TaskMessage;
import org.apache.reef.util.Optional;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

public class HeartbeatHandlerTest {
  /**
   * The current version of heartbeat just contains two metrics
   * - MaxHeapMemory / UsedHeapMemory.
   * @throws Exception
   */
  @Test
  public void testGetMessage() throws Exception {
    // TODO Make sourceId to be set when HeartbeatHandler generated
    // TODO Add metrics manually by users
    final String sourceId = "SourceId";
    HeartbeatHandler hbHandler = new HeartbeatHandler();
    Optional<TaskMessage> op = hbHandler.getMessage();
    assertEquals(sourceId, op.get().getMessageSourceID());
    assertEquals(2, new Codec().decode(op.get().get()).size());
  }
}