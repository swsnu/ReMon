package edu.snu.cms.remon.collector;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serialize/deserialize the Metrics object from/to JSON String
 */
public class MetricSerializer {
  public synchronized static void toStream(Metrics metrics, OutputStream out) throws IOException {
    final DatumWriter<Metrics> rulesWriter = new SpecificDatumWriter<>(Metrics.class);
    final JsonEncoder encoder = EncoderFactory.get().jsonEncoder(Metrics.SCHEMA$, out);
    rulesWriter.write(metrics, encoder);
    encoder.flush();
    out.flush();
  }

  /**
   * Parse Rules from a JSON text InputStream
   */
  public synchronized static Metrics fromStream(InputStream in) throws IOException {
    final DatumReader<Metrics> reader = new SpecificDatumReader<>(Metrics.class);
    final JsonDecoder decoder = DecoderFactory.get().jsonDecoder(Metrics.getClassSchema(), in);
    return reader.read(null, decoder);
  }
}
