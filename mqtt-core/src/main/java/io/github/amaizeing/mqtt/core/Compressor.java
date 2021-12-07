package io.github.amaizeing.mqtt.core;

import io.github.amaizeing.mqtt.core.exception.CompressException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public interface Compressor {

  Compressor NO_COMPRESS = new NoCompressor();
  Compressor ZLIB_COMPRESS = new ZlibCompressor();

  byte[] compress(byte[] data) throws CompressException;

  byte[] decompress(byte[] data) throws CompressException;

  @Slf4j
  final class NoCompressor implements Compressor {

    @Override
    public byte[] compress(final byte[] data) {
      log.info("Before compress: {}. After compress: {}", data.length, data.length);
      return data;
    }

    @Override
    public byte[] decompress(final byte[] data) {
      log.info("Before decompress: {}. After decompress: {}", data.length, data.length);
      return data;
    }

  }

  @Slf4j
  final class ZlibCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] data) throws CompressException {
      try (val outputStream = new ByteArrayOutputStream(data.length)) {
        val deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        val buffer = new byte[1024];
        while (!deflater.finished()) {
          val count = deflater.deflate(buffer);
          outputStream.write(buffer, 0, count);
        }
        deflater.end();
        val result = outputStream.toByteArray();
        log.info("Before compress: {}. After compress: {}", data.length, result.length);
        return result;
      } catch (Exception ex) {
        throw new CompressException(ex);
      }
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressException {
      try (val outputStream = new ByteArrayOutputStream(data.length)) {
        val inflater = new Inflater();
        inflater.setInput(data);

        val buffer = new byte[1024];
        while (!inflater.finished()) {
          val count = inflater.inflate(buffer);
          outputStream.write(buffer, 0, count);
        }
        inflater.end();
        val result = outputStream.toByteArray();
        log.info("Before decompress: {}. After decompress: {}", data.length, result.length);
        return result;
      } catch (Exception ex) {
        throw new CompressException(ex);
      }
    }

  }

}
