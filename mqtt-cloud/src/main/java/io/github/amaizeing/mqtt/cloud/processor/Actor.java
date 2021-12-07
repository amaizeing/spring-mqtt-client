package io.github.amaizeing.mqtt.cloud.processor;

import io.github.amaizeing.mqtt.core.actor.CoreActor;
import io.github.amaizeing.mqtt.core.actor.Processor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
final class Actor extends CoreActor {

  private static final AtomicInteger COUNTER = new AtomicInteger();

  Actor(final List<Processor<?>> processors) {
    super(processors, Executors.newCachedThreadPool(runnable
        -> new Thread(runnable, String.format("actor-%03d", COUNTER.getAndIncrement()))));
  }

}
