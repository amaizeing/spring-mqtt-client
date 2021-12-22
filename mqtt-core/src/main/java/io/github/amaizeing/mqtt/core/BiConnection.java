package io.github.amaizeing.mqtt.core;

import io.github.amaizeing.mqtt.core.exception.MessageBrokerException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BiConnection {

  private final long diffInMillis;

  protected BiConnection() {
    val duration = timeoutOfConnection();
    if (duration == null) {
      throw new NullPointerException("Duration must not be null");
    }
    diffInMillis = duration.toMillis();
    if (diffInMillis <= 0) {
      throw new IllegalArgumentException("Duration must be positive number");
    }
  }

  @PostConstruct
  private void onStart() {
    val client = clientFactory().get();
    val interval = pingInterval().toMillis();
    val executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleWithFixedDelay(() -> {
      try {
        if (!client.isConnected()) {
          return;
        }
        val topics = sendPingTo();
        if (CollectionUtils.isEmpty(topics)) {
          return;
        }
        for (val topic : topics) {
          val message = ConnectionMessage.create(identity());
          client.publish(topic, message);
          log.debug("Send connection request to topics: {} at: {}", topic, Instant.ofEpochMilli(message.getUtc()));
        }
      } catch (MessageBrokerException ex) {
        log.error("Exception while sending ping request to other listeners", ex);
      }
    }, 1_000, interval, TimeUnit.MILLISECONDS);
    log.info("Send ping request each {} ms...", interval);
  }

  protected abstract PubSubClientFactory clientFactory();

  protected abstract Duration pingInterval();

  protected abstract Duration timeoutOfConnection();

  protected abstract String identity();

  protected abstract Collection<String> sendPingTo();

  protected abstract Collection<String> receivePingFrom();

  protected abstract long getReplyTime(String destination);

  protected abstract void setReplyTime(String destination, long utc);

  public abstract void addDestination(String destination);

  public final boolean isConnected(String destination) {
    return diffInMillis - (System.currentTimeMillis() - getReplyTime(destination)) > 0;
  }

  final void process(ConnectionMessage message) {
    setReplyTime(message.getIdentity(), message.getUtc());
  }

}
