package io.github.amaizeing.mqtt.consumer.listener;

import io.github.amaizeing.mqtt.consumer.config.MqttProperties;
import io.github.amaizeing.mqtt.core.Consumer;
import io.github.amaizeing.mqtt.core.MessageBrokerConsumer;
import io.github.amaizeing.mqtt.core.message.UniversityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessageBrokerConsumer
@RequiredArgsConstructor
public class UniversityConsumer extends Consumer<UniversityDto> {

  private final MqttProperties mqttProperties;

  @Override
  public String topic() {
    return mqttProperties.getConsumerProperties().getUniversityTopic();
  }

  @Override
  protected void process(final UniversityDto data) {
    log.info("---------- Receive message: {}", data);
  }

}
