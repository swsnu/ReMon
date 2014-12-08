package edu.snu.cms.remon.collector;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Codec which converts between Metrics and JSON string
 */
public class Codec implements org.apache.reef.io.serialization.Codec<Data> {
  private static Codec codec;

  public static Codec getCodec() {
    if (codec == null) {
      codec = new Codec();
    }
    return codec;
  }

  @Override
  public byte[] encode(Data data) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      DataSerializer.toStream(data, baos);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return baos.toByteArray();
  }

  @Override
  public Data decode(byte[] buf) {
    ByteArrayInputStream bis = new ByteArrayInputStream(buf);
    Data data = null;

    try {
      data = DataSerializer.fromStream(bis);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (data == null)
      return null;

    return data;
  }
}
