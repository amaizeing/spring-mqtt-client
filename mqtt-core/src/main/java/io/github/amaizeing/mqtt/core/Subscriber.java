package io.github.amaizeing.mqtt.core;

import lombok.val;

import java.lang.reflect.ParameterizedType;

public abstract class Subscriber<I> {

  protected abstract String listenOnTopic();

  @SuppressWarnings("unchecked")
  protected Class<I> type() {
    return (Class<I>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  protected boolean retryOnError() {
    return false;
  }

  final void consume(final Serializer serializer, final String messageId, byte[] data, Completer completer) {
    val message = serializer.deserialize(data, type());
    val context = new SubscriberContext<>(messageId, message, completer);
    process(context);
  }

  protected abstract void process(SubscriberContext<I> subscriberContext);

}
