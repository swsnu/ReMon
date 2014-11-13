package edu.snu.cms.remon.collector;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Codec which converts between Metrics and JSON string
 */
public class Codec implements org.apache.reef.io.serialization.Codec<List<Metric>> {

  @Override
  public byte[] encode(List<Metric> obj) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      MetricSerializer.toStream(new Metrics("appId", obj), baos);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return baos.toByteArray();
  }

  @Override
  public List<Metric> decode(byte[] buf) {
    ByteArrayInputStream bis = new ByteArrayInputStream(buf);
    Metrics metrics = null;

    try {
      metrics = MetricSerializer.fromStream(bis);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (metrics == null)
      return null;

    return metrics.getMetrics();
  }
}
