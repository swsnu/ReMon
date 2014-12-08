package edu.snu.cms.remon.collector;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serialize/deserialize the Data object from/to JSON String
 */
public class DataSerializer {
  public synchronized static void toStream(Data data, OutputStream out) throws IOException {
    final DatumWriter<Data> rulesWriter = new SpecificDatumWriter<>(Data.class);
    final JsonEncoder encoder = EncoderFactory.get().jsonEncoder(Data.getClassSchema(), out);
    rulesWriter.write(data, encoder);
    encoder.flush();
    out.flush();
  }

  /**
   * Parse Rules from a JSON text InputStream
   */
  public synchronized static Data fromStream(InputStream in) throws IOException {
    final DatumReader<Data> reader = new SpecificDatumReader<>(Data.class);
    final JsonDecoder decoder = DecoderFactory.get().jsonDecoder(Data.getClassSchema(), in);
    return reader.read(null, decoder);
  }
}
