package io.github.amaizeing.mqtt.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SubscriberContext<T> {

  @Getter
  private final String messageId;
  @Getter
  private final T data;
  private final Completer completer;

  public void complete() {
    completer.complete();
  }

}
