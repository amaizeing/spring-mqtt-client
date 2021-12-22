package io.github.amaizeing.mqtt.local.controller;

import io.github.amaizeing.mqtt.core.PubSubClientFactory;
import io.github.amaizeing.mqtt.core.exception.MessageBrokerException;
import io.github.amaizeing.mqtt.core.exception.SerializeException;
import io.github.amaizeing.mqtt.local.config.MqttProperties;
import io.github.amaizeing.mqtt.local.util.DataFaker;
import io.github.amaizeing.mqtt.local.util.TestUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

  //  private final BiConnection biConnection;
  private final MqttProperties mqttProperties;
  private final PubSubClientFactory clientFactory;

  @SneakyThrows
  @PostMapping("/produce/university")
  public void produceUniversity(@RequestParam int classSize,
      @RequestParam int studentMinSize, @RequestParam int studentMaxSize) {
    val topic = mqttProperties.getProducer().getUniversityTopic() + "/" + mqttProperties.getClient().getClientId();
    run(() -> clientFactory.get()
        .publish(topic, DataFaker.newUniversity(classSize, studentMinSize, studentMaxSize)));
  }

  @SneakyThrows
  @PostMapping("/produce/class")
  public void produceClass(@RequestParam int studentSize) {
    val topic = mqttProperties.getProducer().getClassTopic() + "/" + mqttProperties.getClient().getClientId();
    run(() -> clientFactory.get().publish(topic, DataFaker.newClass(studentSize)));
  }

  @SneakyThrows
  @PostMapping("/produce/big-message")
  public void produceBigMessage() {
    val topic = mqttProperties.getProducer().getBigMessageTopic() + "/" + mqttProperties.getClient().getClientId();
    run(() -> clientFactory.get().publish(topic, TestUtil.BIG_MESSAGE));
  }

//  @GetMapping("/is-connected")
//  public boolean isConnectedToCloud() {
//    return biConnection.isConnected(null);
//  }

  private void run(Runnable runnable) {
    new Thread(() -> {
      try {
        runnable.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }

  interface Runnable {

    void run() throws SerializeException, MessageBrokerException;

  }

}
