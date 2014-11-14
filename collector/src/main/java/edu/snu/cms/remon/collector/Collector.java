package edu.snu.cms.remon.collector;

import org.apache.reef.tang.annotations.Name;
import org.apache.reef.tang.annotations.NamedParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Collector class. This class provides an interface to register
 * the data to observe.
 * More interfaces are on progress to implement
 */
public final class Collector {
  @NamedParameter(doc = "The address of monitor server", short_name = "monitor_addr", default_value="ws://localhost:8080/websocket")
  public class MonitorAddress implements Name<String> {
  }

  private static final Logger LOG = Logger.getLogger(Collector.class.getName());

  public static List<Metric> values = new ArrayList<>();

  /*
   * TODO I think it is better to avoid create a number of Collector object. Instead we may want to keep it as a singleton.
   * But not sure yet this is a right approach.
   */
  private Collector() {
  }

  /**
   * Collects data when the size is up to 30 and
   * flushes the collection there reaches 30 items in the list.
   * @param tag
   * @param value
   */
  public synchronized static void putData(final String sourceId, final String tag, final double value) {
    final long time = System.currentTimeMillis();
    values.add(new Metric(sourceId, tag, time, value));
  }
}
