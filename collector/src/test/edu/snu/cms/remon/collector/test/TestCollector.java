package edu.snu.cms.remon.collector.test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.apache.reef.task.TaskMessage;
import org.apache.reef.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Collector;

/**
 * Unit test for Collector class
 */
public class TestCollector {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    Collector.values.clear();
  }

  /**
   * Unit test for Collector.putData() Test the size of Collector.values after
   * call Collector.putData
   */
  @Test
  public void testPutData() {
    Collector.putData("TestTag", 0.5);
    assertEquals(1, Collector.values.size());
    for (int i = 0; i < 30; i++) {
      Collector.putData("TestTag" + i, 0.5 + i);
    }
    assertEquals(0, Collector.values.size());
  }

  /**
   * Unit test for Collector.heartbeatHandler.GetMessage() Test that
   * getMessage() encode a item in Collector.values correctly
   */
  @Test
  public void testGetMessage() {
    Collector.heartbeatHandler hbHandler = new Collector.heartbeatHandler();
    for (int i = 0; i < 30; i++) {
      Collector.putData("TestTag" + i, 0.5 + i);
    }
    Optional<TaskMessage> op = hbHandler.getMessage();
    assertArrayEquals(op.get().get(), new Codec().encode(Collector.values));
  }
}
