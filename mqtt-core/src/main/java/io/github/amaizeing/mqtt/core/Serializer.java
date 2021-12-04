package io.github.amaizeing.mqtt.core;

import io.github.amaizeing.mqtt.core.exception.SerializeException;

public interface Serializer {

  byte[] serialize(Object message) throws SerializeException;

  <T> T deserialize(byte[] bytes, Class<T> type) throws SerializeException;

}
