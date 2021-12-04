package io.github.amaizeing.mqtt.consumer.config;

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

  private final MqttClientProperties clientProperties;
  private final MqttConsumerProperties consumerProperties;

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
  @ConfigurationProperties(prefix = "message-broker.consumer")
  public static class MqttConsumerProperties {

    private String studentTopic;
    private String universityTopic;

  }

}
