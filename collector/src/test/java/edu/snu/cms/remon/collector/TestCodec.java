package edu.snu.cms.remon.collector;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Codec class
 */
public class TestCodec {
  /**
   * Unit test for Codec.encode() and Codec.decode() Encode and decode a array
   * and test that decoded array is same to original array
   */
  @Test
  public void testEncodeDecode() {
    byte[] bArray;
    List<Metric> metrics;
    List<Message> messages;

    metrics = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      metrics.add(new Metric("app1", "Test" + i, (long) i, 0.5 + i));
    }
    messages = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      messages.add(new Message("app1", "app1" + i, (long) i, "test"));
    }

    Data data = new Data("insert", "app1", metrics, messages);

    bArray = new Codec().encode(null);
    Data decodedvalues = new Codec().decode(bArray);
    assertEquals(decodedvalues, metrics);
  }
}