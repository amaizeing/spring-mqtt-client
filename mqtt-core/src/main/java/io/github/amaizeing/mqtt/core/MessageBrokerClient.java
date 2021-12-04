package io.github.amaizeing.mqtt.core;


public interface MessageBrokerClient {

  <T> void publish(String topic, T message);

  <T> void subscribe(String topic, Consumer<T> onReceive);

}
