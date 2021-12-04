package io.github.amaizeing.mqtt.producer.service;

import io.github.amaizeing.mqtt.core.MessageBrokerClient;
import io.github.amaizeing.mqtt.core.MqttClient;
import io.github.amaizeing.mqtt.core.config.MqttConfig;
import io.github.amaizeing.mqtt.producer.config.MqttProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

  private final MqttProperties mqttProperties;

  private MessageBrokerClient client;

  @PostConstruct
  void init() throws MqttException {
    log.info("Starting MqttService...");
    val mqttConfig = MqttConfig.builder()
        .clientEndpoint(mqttProperties.getClient().getClientEndpoint())
        .clientId(mqttProperties.getClient().getClientId())
        .maxInFlight(mqttProperties.getClient().getMaxInFlight())
        .splitMessage(true)
        .ssl(false)
        .build();
    client = new MqttClient(mqttConfig);
  }

  public void publish(String topic, Object message) {
    client.publish(topic, message);
  }

}
