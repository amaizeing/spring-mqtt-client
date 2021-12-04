package io.github.amaizeing.mqtt.core;

import lombok.val;

import java.lang.reflect.ParameterizedType;

public abstract class Consumer<I> {

  public abstract String topic();

  @SuppressWarnings("unchecked")
  protected Class<I> type() {
    return (Class<I>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  final void consume(final Serializer serializer, final String messageId, byte[] data) {
    val message = serializer.deserialize(data, type());
    process(message);
  }

  protected abstract void process(I data);

}
