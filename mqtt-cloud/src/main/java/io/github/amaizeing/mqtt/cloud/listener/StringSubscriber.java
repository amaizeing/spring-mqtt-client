package io.github.amaizeing.mqtt.cloud.listener;

import io.github.amaizeing.mqtt.core.Subscriber;
import io.github.amaizeing.mqtt.core.SubscriberContext;
import io.github.amaizeing.mqtt.cloud.config.MqttProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StringSubscriber extends Subscriber<String> {

  private final MqttProperties mqttProperties;

  @Override
  public String listenOnTopic() {
    return mqttProperties.getConsumer().getBigMessageTopic() + "/#";
  }

  @SneakyThrows
  @Override
  protected void process(final SubscriberContext<String> subscriberContext) {
    log.info("------------ Receive big string message");
    subscriberContext.complete();
  }

}
