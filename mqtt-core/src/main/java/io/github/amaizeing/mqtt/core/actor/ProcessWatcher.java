package io.github.amaizeing.mqtt.core.actor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ProcessWatcher<T> {

  protected boolean beforeStart(final ProcessorContext<T> processorContext) {
    return true;
  }

  protected abstract void afterComplete(final ProcessorContext<T> processorContext);

  protected void onError(final ProcessorContext<T> processorContext, final Throwable throwable) {
    log.error("Exception while process message: {}", processorContext.getData().getClass(), throwable);
  }

  protected void onFinal(final ProcessorContext<T> processorContext) {
  }


}
