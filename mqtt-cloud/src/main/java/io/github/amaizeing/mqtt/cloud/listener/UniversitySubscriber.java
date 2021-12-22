package io.github.amaizeing.mqtt.cloud.listener;

import io.github.amaizeing.mqtt.cloud.config.MqttProperties;
import io.github.amaizeing.mqtt.core.PubSubClientFactory;
import io.github.amaizeing.mqtt.core.Subscriber;
import io.github.amaizeing.mqtt.cloud.dto.UniversityDto;
import io.github.amaizeing.mqtt.core.SubscriberContext;
import io.github.amaizeing.mqtt.core.actor.CoreActor;
import io.github.amaizeing.mqtt.core.actor.ProcessWatcher;
import io.github.amaizeing.mqtt.core.actor.ProcessorContext;
import io.github.amaizeing.mqtt.cloud.processor.UniversityProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversitySubscriber extends Subscriber<UniversityDto> {

  private final CoreActor actor;
  private final MqttProperties mqttProperties;
  private final PubSubClientFactory clientFactory;

  @Override
  public String listenOnTopic() {
    return mqttProperties.getConsumer().getUniversityTopic() + "/#";
  }

  @SneakyThrows
  @Override
  protected void process(final SubscriberContext<UniversityDto> subscriberContext) {
    val data = subscriberContext.getData();
    log.info("------------ Receive new UniversityDto");

    actor.process(data)
        .processor(UniversityProcessor.class)
        .watcher(new ProcessWatcher<UniversityDto>() {

          @SneakyThrows
          @Override
          public void afterComplete(final ProcessorContext<UniversityDto> processorContext) {
            subscriberContext.complete();
          }
        }).start();

  }

}
