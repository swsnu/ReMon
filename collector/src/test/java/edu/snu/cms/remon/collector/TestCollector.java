package edu.snu.cms.remon.collector;


import org.junit.Test;

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
}
