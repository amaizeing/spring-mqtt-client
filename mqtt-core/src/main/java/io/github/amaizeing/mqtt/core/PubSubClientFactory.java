package io.github.amaizeing.mqtt.core;

@FunctionalInterface
public interface PubSubClientFactory {

  PubSubClient get();

}
