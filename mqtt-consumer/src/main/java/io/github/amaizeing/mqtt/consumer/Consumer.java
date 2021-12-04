package io.github.amaizeing.mqtt.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Consumer {

  public static void main(String[] args) {
    SpringApplication.run(Consumer.class, args);
  }

}
