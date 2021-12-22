package io.github.amaizeing.mqtt.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.amaizeing.mqtt.core.exception.SerializeException;

import java.io.IOException;

public interface Serializer {

  JsonSerializer JSON_SERIALIZER = new JsonSerializer();

  byte[] serialize(Object message) throws SerializeException;

  <T> T deserialize(byte[] bytes, Class<T> type) throws SerializeException;

  final class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public JsonSerializer() {
      objectMapper = new ObjectMapper();
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public JsonSerializer(final ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(final Object message) throws SerializeException {
      try {
        return objectMapper.writeValueAsBytes(message);
      } catch (IOException ex) {
        throw new SerializeException(ex);
      }
    }

    @Override
    public <T> T deserialize(final byte[] bytes, final Class<T> type) throws SerializeException {
      try {
        return objectMapper.readValue(bytes, type);
      } catch (IOException ex) {
        throw new SerializeException(ex);
      }
    }


  }

}
