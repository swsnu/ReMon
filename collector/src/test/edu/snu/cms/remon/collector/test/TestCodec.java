package edu.snu.cms.remon.collector.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.snu.cms.remon.collector.Metric;
import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Collector;

/**
 * Unit test for Codec class
 */
public class TestCodec {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  /**
   * Unit test for Codec.encode() and Codec.decode() Encode and decode a array
   * and test that decoded array is same to original array
   */
  @Test
  public void testEncodeDecode() {
    byte[] bArray;
    List<Metric> values;

    values = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      values.add(new Metric("Test" + i, 0.5 + 1));
    }
    bArray = new Codec().encode(values);
    List<Metric> decodedvalues = new Codec().decode(bArray);
    assertEquals(decodedvalues, values);
  }

}
