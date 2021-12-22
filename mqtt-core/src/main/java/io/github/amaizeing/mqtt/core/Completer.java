package io.github.amaizeing.mqtt.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Completer {

  private boolean completed = false;
  private final Runnable runnable;

  public synchronized void complete() {
    if (completed) {
      return;
    }
    runnable.run();
    completed = true;
  }

}
