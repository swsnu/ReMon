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
    List<Metric> values;

    values = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      values.add(new Metric("Src1", "Test" + i, (long)i, 0.5 + i));
    }
    bArray = new Codec().encode(values);
    List<Metric> decodedvalues = new Codec().decode(bArray);
    assertEquals(decodedvalues, values);
  }
}