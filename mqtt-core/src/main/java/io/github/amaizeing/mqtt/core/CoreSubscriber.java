package io.github.amaizeing.mqtt.core;

import io.github.amaizeing.mqtt.core.exception.MessageBrokerException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.BeanCreationException;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CoreSubscriber {

  private final List<Subscriber<?>> subscribers;
  private final List<BiConnection> biConnections;
  private final PubSubClientFactory clientFactory;

  @PostConstruct
  private void onStart() throws MessageBrokerException {
    for (val consumer : subscribers) {
      val topic = consumer.listenOnTopic();
      clientFactory.get().subscribe(topic, consumer);
      log.info("Consumer: {} is subscribing on topic: {}", consumer.getClass().getName(), topic);
    }

    if (biConnections.isEmpty()) {
      return;
    }

    if (biConnections.size() > 1) {
      throw new BeanCreationException("Allow only 1 ConnectionConsumer bean");
    }

    val biConnection = biConnections.get(0);
    val listeningTopics = biConnection.receivePingFrom();
    for (val topic : listeningTopics) {
      clientFactory.get().subscribe(topic, new Subscriber<ConnectionMessage>() {
        @Override
        protected String listenOnTopic() {
          return topic;
        }

        @Override
        protected Class<ConnectionMessage> type() {
          return ConnectionMessage.class;
        }

        @Override
        protected void process(final SubscriberContext<ConnectionMessage> context) {
          log.debug("Receive ping from topic: {} with identity: {} and time: {}",
              topic, context.getData().getIdentity(), Instant.ofEpochMilli(context.getData().getUtc()));
          biConnection.process(context.getData());
          context.complete();
        }
      });
      log.info("Subscribing on topic: {}", topic);
    }
  }

}
