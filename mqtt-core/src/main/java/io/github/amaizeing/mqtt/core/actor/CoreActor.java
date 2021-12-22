package io.github.amaizeing.mqtt.core.actor;

import io.github.amaizeing.mqtt.core.Pair;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class CoreActor {

  private final Executor executor;
  private final Map<Class<?>, Processor<?>> classToProcessor;
  private final Map<String, Deque<Pair<?, ProcessWatcher<?>>>> keyToMessages;
  private final Object lock;

  protected CoreActor(final List<Processor<?>> processors, ExecutorService executor) {
    classToProcessor = processors.stream().collect(Collectors.toMap(Object::getClass, Function.identity()));
    keyToMessages = new HashMap<>();
    this.executor = executor;
    lock = new Object();
  }

  public final class ActorBuilder<T> {

    private final T message;
    private String key;
    private ProcessWatcher<T> watcher;
    private Class<? extends Processor<T>> processor;

    private ActorBuilder(T message) {
      this.message = message;
    }

    public ActorBuilder<T> key(final String key) {
      this.key = Optional.ofNullable(key).orElse(UUID.randomUUID().toString());
      return this;
    }

    public ActorBuilder<T> processor(final Class<? extends Processor<T>> processor) {
      this.processor = processor;
      return this;
    }

    public ActorBuilder<T> watcher(final ProcessWatcher<T> watcher) {
      this.watcher = watcher;
      return this;
    }

    public void start() {
      run(key, message, processor, watcher);
    }

    private void run(final String key, final T message,
        final Class<? extends Processor<T>> processor,
        final ProcessWatcher<T> watcher) {

      synchronized (lock) {
        val messages = keyToMessages.computeIfAbsent(key, k -> new LinkedBlockingDeque<>());
        messages.addLast(new Pair<>(message, watcher));
        if (messages.size() == 1) {
          process(messages, processor);
        }
      }
    }

  }

  public <T> ActorBuilder<T> process(T message) {
    return new ActorBuilder<>(message);
  }


  @SuppressWarnings("unchecked")
  private <T> void process(final Deque<Pair<?, ProcessWatcher<?>>> messages, final Class<? extends Processor<T>> clazz) {
    val messageAndResult = messages.getFirst();
    if (messageAndResult == null) {
      return;
    }

    val message = (T) messageAndResult.getLeft();
    val result = Optional.ofNullable((ProcessWatcher<T>) messageAndResult.getRight())
        .orElse(new ProcessWatcher<T>() {
          @Override
          protected void afterComplete(final ProcessorContext<T> processorContext) {
            // Do nothing
          }
        });

    CompletableFuture.runAsync(() -> {
          val processorContext = new ProcessorContext<>(message);
          val processor = (Processor<T>) classToProcessor.get(clazz);
          if (processor == null) {
            val ex = new IllegalArgumentException("Not found processor with class: " + message.getClass());
            executor.execute(() -> result.onError(processorContext, ex));
            return;
          }

          try {
            if (result.beforeStart(processorContext)) {
              processor.process(processorContext);
            }
            executor.execute(() -> result.afterComplete(processorContext));
          } catch (Exception ex) {
            executor.execute(() -> result.onError(processorContext, ex));
          } finally {
            messages.removeFirst();
            executor.execute(() -> result.onFinal(processorContext));
          }
        }, executor)
        .whenCompleteAsync((unused, throwable) -> process(messages, clazz), executor);
  }

}
