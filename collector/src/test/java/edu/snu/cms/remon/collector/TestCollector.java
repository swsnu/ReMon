package edu.snu.cms.remon.collector;


import org.apache.reef.task.TaskMessage;
import org.apache.reef.util.Optional;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for Collector class
 */
public class TestCollector {
  /**
   * Unit test for Collector.putData() Test the size of Collector.values after
   * call Collector.putData
   */
  @Test
  public void testPutData() {
    Collector.putData("TestSrc", "TestTag", 0.5);
    assertEquals(1, Collector.values.size());
  }

  /**
   * Unit test for Collector.heartbeatHandler.GetMessage() Test that
   * getMessage() encode a item in Collector.values correctly
   */
  @Ignore
  @Test
  public void testGetMessage() {
    Collector.values.clear();
    Collector.heartbeatHandler hbHandler = new Collector.heartbeatHandler();
    for (int i = 0; i < 30; i++) {
      Collector.putData("TestSrc", "TestTag" + i, 0.5 + i);
    }
    Optional<TaskMessage> op = hbHandler.getMessage();
    assertArrayEquals(op.get().get(), new Codec().encode(Collector.values));
  }
}
