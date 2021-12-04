package io.github.amaizeing.mqtt.producer.controller;

import io.github.amaizeing.mqtt.core.message.StudentDto;
import io.github.amaizeing.mqtt.core.message.UniversityDto;
import io.github.amaizeing.mqtt.producer.config.MqttProperties;
import io.github.amaizeing.mqtt.producer.service.MqttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProduceController {

  private final MqttService mqttService;
  private final MqttProperties mqttProperties;

  @PostMapping("/produce/university")
  public void produceUniversity(@RequestBody UniversityDto message) {
    mqttService.publish(mqttProperties.getProducer().getUniversityTopic(), message);
  }

  @PostMapping("/produce/student")
  public void produceStudent(@RequestBody StudentDto message) {
    mqttService.publish(mqttProperties.getProducer().getStudentTopic(), message);
  }

}
