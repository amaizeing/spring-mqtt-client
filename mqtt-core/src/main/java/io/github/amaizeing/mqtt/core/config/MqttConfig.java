package io.github.amaizeing.mqtt.core.config;


import io.github.amaizeing.mqtt.core.DistributedLock;
import io.github.amaizeing.mqtt.core.Serializer;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.net.SocketFactory;

@Getter
@Builder
@RequiredArgsConstructor
public class MqttConfig {

  private final boolean ssl;
  private final int maxInFlight;
  private final String clientId;
  private final String clientEndpoint;
  private final Serializer serializer;
  private final boolean splitMessage;
  private final SocketFactory socketFactory;
  private final DistributedLock distributedLock;

}
