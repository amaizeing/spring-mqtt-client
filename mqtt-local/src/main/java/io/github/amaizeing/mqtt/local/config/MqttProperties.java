package io.github.amaizeing.mqtt.local.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter(AccessLevel.PACKAGE)
@Configuration
@RequiredArgsConstructor
public class MqttProperties {

  private final MqttClientProperties client;
  private final MqttProducerProperties producer;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  @Configuration
  @ConfigurationProperties(prefix = "message-broker.mqtt")
  public static class MqttClientProperties {

    private int maxInFlight;
    private String clientId;
    private String clientEndpoint;

  }

  @Getter
  @Setter(AccessLevel.PACKAGE)
  @Configuration
  @ConfigurationProperties(prefix = "message-broker.producer")
  public static class MqttProducerProperties {

    private String classTopic;
    private String universityTopic;
    private String bigMessageTopic;

  }

}
