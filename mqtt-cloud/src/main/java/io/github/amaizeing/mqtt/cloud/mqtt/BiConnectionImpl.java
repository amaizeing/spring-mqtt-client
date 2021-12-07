package io.github.amaizeing.mqtt.cloud.mqtt;

import io.github.amaizeing.mqtt.cloud.config.MqttProperties;
import io.github.amaizeing.mqtt.core.BiConnection;
import io.github.amaizeing.mqtt.core.PubSubClientFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(access = AccessLevel.MODULE)
final class BiConnectionImpl extends BiConnection {

  private final MqttProperties mqttProperties;
  private final PubSubClientFactory clientFactory;

  private final Map<String, Long> destinationToLatestUtc = new HashMap<>();

  @PostConstruct
  private void register() {
    addDestination("some-destination");
  }

  @Override
  protected PubSubClientFactory clientFactory() {
    return clientFactory;
  }

  @Override
  protected Duration pingInterval() {
    return Duration.ofSeconds(5);
  }

  @Override
  protected Duration timeoutOfConnection() {
    return Duration.ofSeconds(15);
  }

  @Override
  protected Collection<String> sendPingTo() {
    return destinationToLatestUtc.keySet()
        .stream()
        .map(d -> "cloud.local.connection/" + d)
        .collect(Collectors.toList());
  }

  @Override
  protected Collection<String> receivePingFrom() {
    return Collections.singleton("local.cloud.connection/#");
  }

  @Override
  protected String identity() {
    return mqttProperties.getClient().getClientId();
  }

  @Override
  protected long getReplyTime(final String destination) {
    return destinationToLatestUtc.getOrDefault(destination, 0L);
  }

  @Override
  protected void setReplyTime(final String destination, final long utc) {
    destinationToLatestUtc.put(destination, utc);
  }

  @Override
  public void addDestination(final String destination) {
    destinationToLatestUtc.putIfAbsent(destination, 0L);
  }

}
