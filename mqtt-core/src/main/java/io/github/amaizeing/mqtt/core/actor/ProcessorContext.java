package io.github.amaizeing.mqtt.core.actor;

import lombok.Getter;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

public final class ProcessorContext<D> {

  @Getter
  private final D data;
  private final Map<String, Object> metadata;

  ProcessorContext(D data) {
    this.data = data;
    this.metadata = new HashMap<>();
  }

  public <T> void put(String key, T value) {
    if (value == null) {
      throw new IllegalArgumentException("Does not accept null value");
    }
    val oldVal = metadata.putIfAbsent(key, value);
    if (oldVal != null) {
      throw new IllegalArgumentException("This key already associate with other value");
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) metadata.get(key);
  }

}
