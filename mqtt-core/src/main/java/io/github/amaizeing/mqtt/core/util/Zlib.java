package io.github.amaizeing.mqtt.core.util;

import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class Zlib {

  private Zlib() {
  }

  public static byte[] compress(byte[] data) throws IOException {
    val deflater = new Deflater();
    deflater.setInput(data);
    val outputStream = new ByteArrayOutputStream(data.length);
    deflater.finish();
    val buffer = new byte[1024];
    while (!deflater.finished()) {
      val count = deflater.deflate(buffer);
      outputStream.write(buffer, 0, count);
    }
    outputStream.close();
    return outputStream.toByteArray();
  }

  public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
    val inflater = new Inflater();
    inflater.setInput(data);

    val outputStream = new ByteArrayOutputStream(data.length);
    val buffer = new byte[1024];
    while (!inflater.finished()) {
      val count = inflater.inflate(buffer);
      outputStream.write(buffer, 0, count);
    }
    outputStream.close();
    return outputStream.toByteArray();
  }

}
