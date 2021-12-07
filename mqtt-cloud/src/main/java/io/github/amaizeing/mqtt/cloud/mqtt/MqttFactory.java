package io.github.amaizeing.mqtt.cloud.mqtt;

import io.github.amaizeing.mqtt.core.PubSubClient;
import io.github.amaizeing.mqtt.core.PubSubClientFactory;
import io.github.amaizeing.mqtt.core.MqttClient;
import io.github.amaizeing.mqtt.core.Compressor;
import io.github.amaizeing.mqtt.core.config.MqttConfig;
import io.github.amaizeing.mqtt.cloud.config.MqttProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
final class MqttFactory implements PubSubClientFactory {

  private final PubSubClient client;

  MqttFactory(MqttProperties mqttProperties) throws MqttException {
    val mqttConfig = MqttConfig.builder()
        .clientEndpoint(mqttProperties.getClient().getClientEndpoint())
        .clientId(mqttProperties.getClient().getClientId())
        .maxInFlight(mqttProperties.getClient().getMaxInFlight())
        .compressor(Compressor.ZLIB_COMPRESS)
        .build();
    client = new MqttClient(mqttConfig);
  }

  @Override
  public PubSubClient get() {
    return client;
  }

}
