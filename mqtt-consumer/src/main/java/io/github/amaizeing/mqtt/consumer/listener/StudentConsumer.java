package io.github.amaizeing.mqtt.consumer.listener;

import io.github.amaizeing.mqtt.consumer.config.MqttProperties;
import io.github.amaizeing.mqtt.core.Consumer;
import io.github.amaizeing.mqtt.core.MessageBrokerConsumer;
import io.github.amaizeing.mqtt.core.message.StudentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessageBrokerConsumer
@RequiredArgsConstructor
public class StudentConsumer extends Consumer<StudentDto> {

  private final MqttProperties mqttProperties;

  @Override
  public String topic() {
    return mqttProperties.getConsumerProperties().getStudentTopic();
  }

  @Override
  protected void process(final StudentDto data) {
    log.info("---------- Receive message: {}", data);
  }

}
