package io.github.amaizeing.mqtt.local.mqtt;

import io.github.amaizeing.mqtt.core.BiConnection;
import io.github.amaizeing.mqtt.core.PubSubClientFactory;
import io.github.amaizeing.mqtt.local.config.MqttProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor(access = AccessLevel.MODULE)
final class BiConnectionImpl extends BiConnection {

  private final MqttProperties mqttProperties;
  private final PubSubClientFactory clientFactory;

  private long latestUtc = 0;

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
    return Collections.singleton("local.cloud.connection/" + identity());
  }

  @Override
  protected Collection<String> receivePingFrom() {
    return Collections.singleton("cloud.local.connection/" + identity());
  }

  @Override
  protected String identity() {
    return mqttProperties.getClient().getClientId();
  }

  @Override
  protected long getReplyTime(final String destination) {
    return latestUtc;
  }

  @Override
  protected void setReplyTime(final String destination, final long utc) {
    latestUtc = utc;
  }

  @Override
  public void addDestination(final String destination) {
    throw new UnsupportedOperationException();
  }

}
