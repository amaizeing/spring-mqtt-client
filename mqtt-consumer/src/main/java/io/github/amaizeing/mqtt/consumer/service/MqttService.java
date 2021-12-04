package io.github.amaizeing.mqtt.consumer.service;

import io.github.amaizeing.mqtt.consumer.config.MqttProperties;
import io.github.amaizeing.mqtt.core.Consumer;
import io.github.amaizeing.mqtt.core.MessageBrokerClient;
import io.github.amaizeing.mqtt.core.MqttClient;
import io.github.amaizeing.mqtt.core.config.MqttConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

  private final List<Consumer<?>> consumers;
  private final MqttProperties mqttProperties;

  private MessageBrokerClient client;

  @PostConstruct
  void init() throws MqttException {
    val mqttConfig = MqttConfig.builder()
        .clientEndpoint(mqttProperties.getClientProperties().getClientEndpoint())
        .clientId(mqttProperties.getClientProperties().getClientId())
        .maxInFlight(mqttProperties.getClientProperties().getMaxInFlight())
        .ssl(false)
        .build();
    client = new MqttClient(mqttConfig);

    for (val consumer : consumers) {
      client.subscribe(consumer.topic(), consumer);
    }
  }

  public void publish(String topic, Object message) {
    client.publish(topic, message);
  }

}
