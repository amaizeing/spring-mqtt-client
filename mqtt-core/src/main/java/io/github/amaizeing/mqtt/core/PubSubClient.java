package io.github.amaizeing.mqtt.core;


import io.github.amaizeing.mqtt.core.exception.MessageBrokerException;
import io.github.amaizeing.mqtt.core.exception.SerializeException;

import java.util.Collection;

public interface PubSubClient {

  boolean isConnected();

  <O> void publish(String topic, O message) throws IllegalArgumentException, SerializeException, MessageBrokerException;

  <O> void subscribe(String topic, Subscriber<O> onReceive) throws SerializeException, MessageBrokerException;

  void unsubscribe(String topic) throws MessageBrokerException;

  void unsubscribe(Collection<String> topics) throws MessageBrokerException;

}
