package io.github.amaizeing.mqtt.local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class LocalApplication {

  public static void main(String[] args) {
    SpringApplication.run(LocalApplication.class, args);
  }

}
