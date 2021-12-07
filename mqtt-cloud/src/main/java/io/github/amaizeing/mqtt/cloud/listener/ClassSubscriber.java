package io.github.amaizeing.mqtt.cloud.listener;

import io.github.amaizeing.mqtt.cloud.config.MqttProperties;
import io.github.amaizeing.mqtt.cloud.dto.ClassDto;
import io.github.amaizeing.mqtt.cloud.processor.ClassProcessor;
import io.github.amaizeing.mqtt.core.Subscriber;
import io.github.amaizeing.mqtt.core.SubscriberContext;
import io.github.amaizeing.mqtt.core.actor.CoreActor;
import io.github.amaizeing.mqtt.core.actor.ProcessWatcher;
import io.github.amaizeing.mqtt.core.actor.ProcessorContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassSubscriber extends Subscriber<ClassDto> {

  private final CoreActor actor;
  private final MqttProperties mqttProperties;
  private final StringRedisTemplate stringRedisTemplate;

  private static final String PROCESS_KEY = "process.class-dto";

  @Override
  public String listenOnTopic() {
    return mqttProperties.getConsumer().getClassTopic() + "/#";
  }

  @SneakyThrows
  @Override
  protected void process(final SubscriberContext<ClassDto> subscriberContext) {
    val data = subscriberContext.getData();
    log.info("------------ Receive new ClassDto");

    val recordCount = stringRedisTemplate.opsForSet()
        .add(PROCESS_KEY, subscriberContext.getMessageId());
    if (recordCount == null || recordCount == 0) {
      log.info("Message has been processing by other instance");
      subscriberContext.complete();
      return;
    }

    actor.process(data)
        .key("data.getName()")
        .processor(ClassProcessor.class)
        .watcher(new ProcessWatcher<ClassDto>() {

          @Override
          protected boolean beforeStart(final ProcessorContext<ClassDto> processorContext) {
            processorContext.put("key", "value");
            return true;
          }

          @Override
          protected void afterComplete(final ProcessorContext<ClassDto> processorContext) {
            // Send ack to Message broker server
            subscriberContext.complete();
          }

          @Override
          protected void onError(final ProcessorContext<ClassDto> processorContext, final Throwable throwable) {
            log.error("Exception while process message: {}", processorContext.getData().getClass(), throwable);
          }

          @Override
          protected void onFinal(final ProcessorContext<ClassDto> processorContext) {
            stringRedisTemplate.opsForSet().remove(PROCESS_KEY, subscriberContext.getMessageId());
          }
        }).start();

  }

}
